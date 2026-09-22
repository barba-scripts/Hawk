package com.fiap.hawk.controller;

import com.fiap.hawk.dto.request.DashboardInsightsRequest;
import com.fiap.hawk.dto.response.DashboardAnalysisResponse;
import com.fiap.hawk.dto.response.DashboardInsightsResponse;
import com.fiap.hawk.dto.response.DashboardSummaryResponse;
import com.fiap.hawk.exception.ApiException;
import com.fiap.hawk.repository.UserRepository;
import com.fiap.hawk.security.JwtAuthenticationFilter;
import com.fiap.hawk.security.JwtService;
import com.fiap.hawk.security.SecurityConfig;
import com.fiap.hawk.service.DashboardInsightsService;
import com.fiap.hawk.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class DashboardControllerTests {

	@Autowired
	private MockMvc mvc;
	@MockitoBean
	private DashboardService dashboard;
	@MockitoBean
	private DashboardInsightsService insights;
	@MockitoBean
	private JwtService jwtService;
	@MockitoBean
	private UserRepository users;

	@Test
	@WithMockUser(roles = "LIDER")
	void leaderCanGenerateInsightsWithFilters() throws Exception {
		when(insights.generate(any())).thenReturn(new DashboardInsightsResponse(Instant.parse("2026-09-22T00:00:00Z"),
				new DashboardAnalysisResponse("Análise gerencial.", List.of(), List.of(), List.of(), List.of())));
		mvc.perform(post("/api/v1/dashboard/insights").contentType(MediaType.APPLICATION_JSON)
				.content("{\"division\":\"Logística\",\"from\":\"2026-01-01\",\"to\":\"2026-09-30\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.generatedAt").value("2026-09-22T00:00:00Z"))
				.andExpect(jsonPath("$.analysis.summary").value("Análise gerencial."))
				.andExpect(jsonPath("$.analysis.recommendations").isArray())
				.andExpect(jsonPath("$.prompt").doesNotExist());
		verify(insights).generate(new DashboardInsightsRequest("Logística", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 9, 30)));
	}

	@Test
	@WithMockUser(roles = "LIDER")
	void acceptsEmptyFilters() throws Exception {
		mvc.perform(post("/api/v1/dashboard/insights").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isOk());
		verify(insights).generate(new DashboardInsightsRequest(null, null, null));
	}

	@Test
	void anonymousUserCannotGenerateInsights() throws Exception {
		mvc.perform(post("/api/v1/dashboard/insights").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isUnauthorized());
		verifyNoInteractions(insights);
	}

	@ParameterizedTest
	@ValueSource(strings = {"OPERADOR", "GESTOR"})
	void otherRolesCannotGenerateInsights(String role) throws Exception {
		mvc.perform(post("/api/v1/dashboard/insights").with(user("test").roles(role))
				.contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isForbidden());
		verifyNoInteractions(insights);
	}

	@ParameterizedTest
	@ValueSource(strings = {"{", "null", "", "{\"from\":\"invalid\"}",
			"{\"from\":\"2026-09-30\",\"to\":\"2026-01-01\"}"})
	@WithMockUser(roles = "LIDER")
	void rejectsInvalidRequest(String body) throws Exception {
		mvc.perform(post("/api/v1/dashboard/insights").contentType(MediaType.APPLICATION_JSON).content(body))
				.andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
		verifyNoInteractions(insights);
	}

	@Test
	@WithMockUser(roles = "LIDER")
	void rejectsOversizedDivision() throws Exception {
		mvc.perform(post("/api/v1/dashboard/insights").contentType(MediaType.APPLICATION_JSON)
				.content("{\"division\":\"" + "a".repeat(101) + "\"}"))
				.andExpect(status().isBadRequest());
		verifyNoInteractions(insights);
	}

	@Test
	@WithMockUser(roles = "LIDER")
	void providerFailureUsesGlobalErrorFormatAndSummaryStillWorks() throws Exception {
		when(insights.generate(any())).thenThrow(new ApiException("AI_UNAVAILABLE", "IA indisponível.", HttpStatus.SERVICE_UNAVAILABLE));
		mvc.perform(post("/api/v1/dashboard/insights").contentType(MediaType.APPLICATION_JSON).content("{}"))
				.andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.code").value("AI_UNAVAILABLE"))
				.andExpect(jsonPath("$.traceId").isNotEmpty());
		when(dashboard.summary(null, null, null)).thenReturn(
				new DashboardSummaryResponse(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, java.util.List.of()));
		mvc.perform(get("/api/v1/dashboard/summary"))
				.andExpect(status().isOk()).andExpect(jsonPath("$.ideasApproved").value(8));
	}
}
