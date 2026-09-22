package com.fiap.hawk.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateProjetoRequest(
		String name,
		String description,
		String division,
		String stage,
		String status,
		@Min(0) @Max(100) Integer progress,
		BigDecimal investment,
		LocalDate deadline,
		BigDecimal financialReturn,
		BigDecimal costReduction,
		BigDecimal productivity,
		String observation
) {
}
