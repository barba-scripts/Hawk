package com.fiap.hawk.dto.response;

import java.util.List;

public record DashboardSummaryResponse(
		double totalInvestment,
		double totalReturn,
		double averageRoi,
		double averageCostReduction,
		double averageProductivity,
		long activeProjects,
		long completedProjects,
		long ideasApproved,
		long ideasInAnalysis,
		long totalProjects,
		List<ProjectsByDivision> projectsByDivision
) {
	public record ProjectsByDivision(String division, long count) {
	}
}
