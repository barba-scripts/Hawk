package com.fiap.hawk.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ReviewIdeiaRequest(
		@NotBlank(message = "Informe o status.") String status,
		@Min(0) @Max(100) Integer score,
		String observation
) {
}
