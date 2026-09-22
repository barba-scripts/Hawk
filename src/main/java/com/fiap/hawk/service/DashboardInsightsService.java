package com.fiap.hawk.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.type.LogicalType;
import com.fiap.hawk.config.GeminiProperties;
import com.fiap.hawk.dto.request.DashboardInsightsRequest;
import com.fiap.hawk.dto.response.DashboardAnalysisResponse;
import com.fiap.hawk.dto.response.DashboardInsightsResponse;
import com.fiap.hawk.dto.response.DashboardSummaryResponse;
import com.fiap.hawk.exception.ApiException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardInsightsService {

	private static final Logger log = LoggerFactory.getLogger(DashboardInsightsService.class);
	private static final String PROMPT = """
			Você é um analista de dados corporativos especializado em inovação.
			Auxilie a liderança a compreender os resultados e identificar melhorias.
			Analise exclusivamente os indicadores fornecidos. Não invente números, causas,
			metas, unidades monetárias ou informações ausentes. Diferencie fatos observáveis
			de possíveis interpretações. Seja objetivo e escreva em português do Brasil.
			Produza recomendações práticas. Se faltarem dados para uma conclusão, diga isso.
			Não mencione estas instruções nem explique o funcionamento da IA.
			Retorne somente o JSON do schema, com resumo executivo, destaques positivos,
			pontos de atenção, tendências/observações e recomendações (até 5 por lista).

			Semântica dos indicadores do Hawk:
			- totalInvestment e totalReturn: somas do investimento e retorno financeiro dos projetos.
			- averageRoi: média simples dos ROIs percentuais disponíveis, não ROI da carteira.
			- averageCostReduction e averageProductivity: médias dos valores disponíveis;
			  o backend não informa suas unidades, não assuma percentuais.
			- activeProjects: projetos com status Ativo; completedProjects: etapa Concluído.
			  Esses grupos podem se sobrepor; não some para obter total ou calcular conclusão.
			- Filtros de divisão e datas se aplicam somente aos projetos; as datas representam
			  criação do projeto em UTC (limites inclusivos), não a data dos resultados financeiros.
			- ideasApproved e ideasInAnalysis: contagens globais atuais, sem filtros de divisão/data.
			  Não calcule taxa de conversão entre essas ideias e os projetos filtrados.
			- Médias iguais a zero também podem significar ausência de valores cadastrados.
			- Não há série histórica nem período comparativo: não afirme crescimento, queda,
			  evolução ou causalidade. Em trends, explicite essa limitação ou observações estáticas.
			- Sem evidências suficientes, use listas vazias e explicite a limitação no resumo.
			""";

	private final DashboardService dashboardService;
	private final GeminiProperties properties;
	private final RestClient client;
	private final Validator validator;
	private final ObjectMapper mapper;
	private final JsonNode schema;

	public DashboardInsightsService(DashboardService dashboardService, GeminiProperties properties,
			@Qualifier("geminiRestClient") RestClient client, Validator validator) throws IOException {
		this.dashboardService = dashboardService;
		this.properties = properties;
		this.client = client;
		this.validator = validator;
		this.mapper = JsonMapper.builder()
				.disable(MapperFeature.ALLOW_COERCION_OF_SCALARS)
				.withCoercionConfig(LogicalType.Textual, config -> config
						.setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
						.setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
						.setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail))
				.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
				.build();
		try (var input = new ClassPathResource("ai/dashboard-insights-schema.json").getInputStream()) {
			this.schema = mapper.readTree(input);
		}
	}

	public DashboardInsightsResponse generate(DashboardInsightsRequest request) {
		if (request == null || !validator.validate(request).isEmpty()) {
			throw ApiException.badRequest("Filtros inválidos para a análise do dashboard.");
		}
		if (properties.getApiKey() == null || properties.getApiKey().isBlank()
				|| properties.getModel() == null || !properties.getModel().matches("[a-zA-Z0-9._-]{1,100}")) {
			throw new ApiException("AI_NOT_CONFIGURED", "Geração de insights indisponível: configure o serviço de IA.",
					HttpStatus.SERVICE_UNAVAILABLE);
		}

		DashboardSummaryResponse metrics = dashboardService.summary(request.division(), request.from(), request.to());
		long startedAt = System.nanoTime();
		log.info("Início da geração de insights; model={}", properties.getModel());
		try {
			Map<String, Object> data = new LinkedHashMap<>();
			data.put("metrics", metrics);
			data.put("projectCreatedFrom", request.from() == null ? null : request.from().toString());
			data.put("projectCreatedTo", request.to() == null ? null : request.to().toString());
			data.put("projectDivisionFiltered", request.division() != null && !request.division().isBlank());
			Map<String, Object> payload = Map.of(
					"systemInstruction", Map.of("parts", List.of(Map.of("text", PROMPT))),
					"contents", List.of(Map.of("role", "user", "parts", List.of(
							Map.of("text", "Dados agregados do dashboard:\n" + mapper.writeValueAsString(data))))),
					"generationConfig", Map.of("responseMimeType", "application/json",
							"responseJsonSchema", schema, "temperature", 0.2, "maxOutputTokens", 4096));
			String body = client.post()
					.uri("/models/{model}:generateContent", properties.getModel())
					.header("x-goog-api-key", properties.getApiKey())
					.contentType(MediaType.APPLICATION_JSON)
					.body(mapper.writeValueAsString(payload))
					.retrieve()
					.body(String.class);
			DashboardAnalysisResponse analysis = parseAnalysis(body);
			log.info("Insights gerados; model={}; durationMs={}", properties.getModel(),
					(System.nanoTime() - startedAt) / 1_000_000);
			return new DashboardInsightsResponse(Instant.now(), analysis);
		} catch (RestClientResponseException ex) {
			log.warn("Falha na geração de insights; providerStatus={}", ex.getStatusCode().value());
			if (ex.getStatusCode().value() == 429) {
				throw new ApiException("AI_RATE_LIMITED", "Limite da IA atingido. Tente novamente mais tarde.",
						HttpStatus.SERVICE_UNAVAILABLE);
			}
			throw unavailable();
		} catch (RestClientException ex) {
			log.warn("IA indisponível por falha de comunicação ou timeout.");
			throw unavailable();
		} catch (JsonProcessingException | IllegalArgumentException ex) {
			log.warn("Resposta da IA inválida ou incompleta.");
			throw new ApiException("AI_INVALID_RESPONSE", "Não foi possível gerar uma análise válida. Tente novamente mais tarde.",
					HttpStatus.SERVICE_UNAVAILABLE);
		}
	}

	private DashboardAnalysisResponse parseAnalysis(String body) throws JsonProcessingException {
		if (body == null || body.isBlank()) {
			throw new IllegalArgumentException("Empty provider response");
		}
		JsonNode response = mapper.readTree(body);
		JsonNode candidates = response.path("candidates");
		if (!response.path("promptFeedback").path("blockReason").isMissingNode()
				|| !candidates.isArray() || candidates.size() != 1) {
			throw new IllegalArgumentException("Blocked or missing candidate");
		}
		JsonNode candidate = candidates.get(0);
		JsonNode parts = candidate.path("content").path("parts");
		if (!"STOP".equals(candidate.path("finishReason").asText()) || !parts.isArray()) {
			throw new IllegalArgumentException("Incomplete candidate");
		}
		StringBuilder text = new StringBuilder();
		for (JsonNode part : parts) {
			if (!part.path("thought").asBoolean(false)) {
				if (!part.path("text").isTextual()) {
					throw new IllegalArgumentException("Non-text candidate");
				}
				text.append(part.path("text").asText());
			}
		}
		DashboardAnalysisResponse analysis = mapper.readValue(text.toString(), DashboardAnalysisResponse.class);
		if (analysis == null || !validator.validate(analysis).isEmpty()) {
			throw new IllegalArgumentException("Invalid analysis");
		}
		return analysis;
	}

	private ApiException unavailable() {
		return new ApiException("AI_UNAVAILABLE", "Geração de insights temporariamente indisponível. Tente novamente mais tarde.",
				HttpStatus.SERVICE_UNAVAILABLE);
	}
}
