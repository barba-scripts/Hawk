package com.fiap.hawk.dto.response;

public record DashboardSummaryResponse(
		double totalInvestment,
		double totalReturn,
		double averageRoi,
		double averageCostReduction,
		double averageProductivity,
		long activeProjects,
		long completedProjects,
		long ideasApproved,
		long ideasInAnalysis
) {
}
