package com.fiap.hawk.controller;

import com.fiap.hawk.dto.request.DashboardInsightsRequest;
import com.fiap.hawk.dto.response.DashboardInsightsResponse;
import com.fiap.hawk.dto.response.DashboardSummaryResponse;
import com.fiap.hawk.service.DashboardInsightsService;
import com.fiap.hawk.service.DashboardService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

	private final DashboardService dashboardService;
	private final DashboardInsightsService insightsService;

	public DashboardController(DashboardService dashboardService, DashboardInsightsService insightsService) {
		this.dashboardService = dashboardService;
		this.insightsService = insightsService;
	}

	@PostMapping("/insights")
	public DashboardInsightsResponse insights(@Valid @RequestBody DashboardInsightsRequest request) {
		return insightsService.generate(request);
	}

	@GetMapping("/summary")
	public DashboardSummaryResponse summary(
			@RequestParam(required = false) String division,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return dashboardService.summary(division, from, to);
	}
}
