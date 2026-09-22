package com.fiap.hawk.dto.response;

import java.time.Instant;

public record DashboardInsightsResponse(Instant generatedAt, DashboardAnalysisResponse analysis) {
}
