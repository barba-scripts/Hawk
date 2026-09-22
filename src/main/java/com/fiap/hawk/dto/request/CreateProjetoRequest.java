package com.fiap.hawk.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateProjetoRequest(
		@NotBlank String name,
		String description,
		@NotBlank String division,
		@NotBlank String stage,
		@NotBlank String status,
		@NotNull BigDecimal investment,
		@NotNull LocalDate deadline,
		@NotNull Integer ideaId
) {
}
