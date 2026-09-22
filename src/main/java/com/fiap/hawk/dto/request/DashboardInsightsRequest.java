package com.fiap.hawk.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record DashboardInsightsRequest(
		@Size(max = 100) String division,
		LocalDate from,
		LocalDate to
) {

	@AssertTrue(message = "from deve ser anterior ou igual a to.")
	public boolean isPeriodValid() {
		return from == null || to == null || !from.isAfter(to);
	}
}
