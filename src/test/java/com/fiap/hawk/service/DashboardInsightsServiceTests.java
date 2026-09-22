package com.fiap.hawk.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.hawk.config.GeminiProperties;
import com.fiap.hawk.dto.request.DashboardInsightsRequest;
import com.fiap.hawk.dto.response.DashboardSummaryResponse;
import com.fiap.hawk.exception.ApiException;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.net.SocketTimeoutException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class DashboardInsightsServiceTests {

	private static final String URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent";
	private static final String ANALYSIS = """
			{"summary":"Há projetos ativos no recorte analisado.",
			 "highlights":[{"title":"Projetos ativos","description":"Há 3 projetos ativos."}],
			 "attentionPoints":[{"title":"Dados limitados","description":"Não há histórico.","severity":"low"}],
			 "trends":[],
			 "recommendations":[{"title":"Acompanhar","description":"Registrar resultados periodicamente.","priority":"high"}]}
			""";
	private static final DashboardInsightsRequest FILTERS = new DashboardInsightsRequest(
			"Divisão interna", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 9, 30));
	private final ObjectMapper mapper = new ObjectMapper();
	private DashboardService dashboard;
	private GeminiProperties properties;
	private ValidatorFactory validation;
	private MockRestServiceServer server;
	private DashboardInsightsService service;

	@BeforeEach
	void setUp() throws Exception {
		dashboard = mock(DashboardService.class);
		properties = new GeminiProperties();
		properties.setApiKey("test-key");
		validation = Validation.buildDefaultValidatorFactory();
		RestClient.Builder builder = RestClient.builder().baseUrl("https://generativelanguage.googleapis.com/v1beta");
		server = MockRestServiceServer.bindTo(builder).build();
		service = new DashboardInsightsService(dashboard, properties, builder.build(), validation.getValidator());
		when(dashboard.summary(any(), any(), any())).thenReturn(
				new DashboardSummaryResponse(1000, 1500, 50, 10, 20, 3, 1, 8, 2, 4, java.util.List.of()));
	}

	@AfterEach
	void tearDown() {
		validation.close();
		server.verify();
	}

	@Test
	void generatesValidatedInsightsUsingExistingSummaryAndOnlyAggregates() throws Exception {
		server.expect(requestTo(URL)).andExpect(method(HttpMethod.POST))
				.andExpect(header("x-goog-api-key", "test-key"))
				.andExpect(content().contentType(MediaType.APPLICATION_JSON))
				.andExpect(request -> {
					String body = ((MockClientHttpRequest) request).getBodyAsString();
					assertThat(body).doesNotContain("Divisão interna", "test-key", "password", "email", "managerUserId");
					JsonNode payload = mapper.readTree(body);
					assertThat(payload.at("/generationConfig/responseMimeType").asText()).isEqualTo("application/json");
					assertThat(payload.at("/generationConfig/responseJsonSchema/required").size()).isEqualTo(5);
					assertThat(payload.at("/systemInstruction/parts/0/text").asText())
							.contains("contagens globais", "Não há série histórica", "português do Brasil");
					String dataText = payload.at("/contents/0/parts/0/text").asText();
					JsonNode data = mapper.readTree(dataText.substring(dataText.indexOf('\n') + 1));
					assertThat(data.path("metrics").size()).isEqualTo(9);
					assertThat(data.at("/metrics/totalInvestment").asDouble()).isEqualTo(1000);
					assertThat(data.path("projectCreatedFrom").asText()).isEqualTo("2026-01-01");
					assertThat(data.path("projectDivisionFiltered").asBoolean()).isTrue();
				})
				.andRespond(withSuccess(envelope(ANALYSIS), MediaType.APPLICATION_JSON));
		Instant before = Instant.now();
		var result = service.generate(FILTERS);
		assertThat(result.analysis().summary()).isEqualTo("Há projetos ativos no recorte analisado.");
		assertThat(result.analysis().recommendations().getFirst().priority()).isEqualTo("high");
		assertThat(result.generatedAt()).isBetween(before, Instant.now());
		verify(dashboard).summary(FILTERS.division(), FILTERS.from(), FILTERS.to());
		verifyNoMoreInteractions(dashboard);
	}

	@ParameterizedTest
	@ValueSource(strings = {"not json", "null", "{}",
			"{\"summary\":\"ok\",\"highlights\":[],\"attentionPoints\":[],\"trends\":[]}",
			"{\"summary\":42,\"highlights\":[],\"attentionPoints\":[],\"trends\":[],\"recommendations\":[]}",
			"{\"summary\":\" \",\"highlights\":[],\"attentionPoints\":[],\"trends\":[],\"recommendations\":[]}",
			"{\"summary\":\"ok\",\"highlights\":[null],\"attentionPoints\":[],\"trends\":[],\"recommendations\":[]}"})
	void rejectsMalformedOrIncompleteAnalysis(String analysis) throws Exception {
		server.expect(requestTo(URL)).andRespond(withSuccess(envelope(analysis), MediaType.APPLICATION_JSON));
		assertFailure("AI_INVALID_RESPONSE");
	}

	@ParameterizedTest
	@ValueSource(strings = {"{", "null", "{}", "{\"candidates\":[]}",
			"{\"promptFeedback\":{\"blockReason\":\"SAFETY\"}}",
			"{\"candidates\":[{\"finishReason\":\"MAX_TOKENS\",\"content\":{\"parts\":[{\"text\":\"{}\"}]}}]}"})
	void rejectsInvalidBlockedOrTruncatedEnvelope(String body) {
		server.expect(requestTo(URL)).andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
		assertFailure("AI_INVALID_RESPONSE");
	}

	@Test
	void rejectsInvalidSeverity() throws Exception {
		server.expect(requestTo(URL)).andRespond(withSuccess(envelope(ANALYSIS.replace("\"low\"", "\"critical\"")), MediaType.APPLICATION_JSON));
		assertFailure("AI_INVALID_RESPONSE");
	}

	@Test
	void rejectsUnknownFieldsAndTrailingJson() throws Exception {
		server.expect(requestTo(URL)).andRespond(withSuccess(envelope(ANALYSIS.replace("\"trends\":[]", "\"trends\":[],\"unexpected\":true")), MediaType.APPLICATION_JSON));
		assertFailure("AI_INVALID_RESPONSE");
		server.reset();
		server.expect(requestTo(URL)).andRespond(withSuccess(envelope(ANALYSIS + " {}"), MediaType.APPLICATION_JSON));
		assertFailure("AI_INVALID_RESPONSE");
	}

	@Test
	void rejectsTooManyItems() throws Exception {
		var body = mapper.readTree(ANALYSIS);
		var highlights = (com.fasterxml.jackson.databind.node.ArrayNode) body.path("highlights");
		for (int i = 0; i < 5; i++) {
			highlights.add(highlights.get(0).deepCopy());
		}
		server.expect(requestTo(URL)).andRespond(withSuccess(envelope(mapper.writeValueAsString(body)), MediaType.APPLICATION_JSON));
		assertFailure("AI_INVALID_RESPONSE");
	}

	@ParameterizedTest
	@ValueSource(ints = {400, 401, 403, 404, 500, 502, 503})
	void sanitizesProviderErrors(int status) {
		server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.valueOf(status))
				.body("<html>provider secret</html>").contentType(MediaType.TEXT_HTML));
		assertFailure("AI_UNAVAILABLE");
	}

	@Test
	void reportsRateLimitWithoutRetrying() {
		server.expect(requestTo(URL)).andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
		assertFailure("AI_RATE_LIMITED");
	}

	@Test
	void reportsTimeoutWithoutRetrying() {
		server.expect(requestTo(URL)).andRespond(withException(new SocketTimeoutException("provider secret")));
		assertFailure("AI_UNAVAILABLE");
	}

	@Test
	void reportsEmptyResponse() {
		server.expect(requestTo(URL)).andRespond(withNoContent());
		assertFailure("AI_INVALID_RESPONSE");
	}

	@Test
	void missingKeyDoesNotQueryDashboardOrProvider() {
		properties.setApiKey(" ");
		assertFailure("AI_NOT_CONFIGURED");
		verifyNoInteractions(dashboard);
	}

	@Test
	void invalidModelDoesNotQueryDashboardOrProvider() {
		properties.setModel("https://another-host.example/model");
		assertFailure("AI_NOT_CONFIGURED");
		verifyNoInteractions(dashboard);
	}

	@Test
	void invalidFiltersFailBeforeReadingMetrics() {
		assertThatThrownBy(() -> service.generate(new DashboardInsightsRequest(null, FILTERS.to(), FILTERS.from())))
				.isInstanceOfSatisfying(ApiException.class, ex -> assertThat(ex.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST));
		verifyNoInteractions(dashboard);
	}

	@Test
	void acceptsEmptyDataWithoutInventedFallback() throws Exception {
		when(dashboard.summary(null, null, null)).thenReturn(
				new DashboardSummaryResponse(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, java.util.List.of()));
		String analysis = "{\"summary\":\"Dados insuficientes.\",\"highlights\":[],\"attentionPoints\":[],\"trends\":[],\"recommendations\":[]}";
		server.expect(requestTo(URL)).andRespond(withSuccess(envelope(analysis), MediaType.APPLICATION_JSON));
		assertThat(service.generate(new DashboardInsightsRequest(null, null, null)).analysis().summary())
				.isEqualTo("Dados insuficientes.");
	}

	private String envelope(String analysis) throws Exception {
		return mapper.writeValueAsString(Map.of("candidates", List.of(Map.of("finishReason", "STOP",
				"content", Map.of("parts", List.of(Map.of("text", analysis)))))));
	}

	private void assertFailure(String code) {
		assertThatThrownBy(() -> service.generate(FILTERS)).isInstanceOfSatisfying(ApiException.class, ex -> {
			assertThat(ex.getStatus()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
			assertThat(ex.getCode()).isEqualTo(code);
			assertThat(ex.getMessage()).doesNotContain("test-key", "provider secret", "<html>");
		});
	}
}
