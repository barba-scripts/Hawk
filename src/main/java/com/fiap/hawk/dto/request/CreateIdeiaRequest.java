package com.fiap.hawk.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateIdeiaRequest(
		@NotBlank(message = "Informe um título.") String title,
		@NotBlank(message = "Informe uma descrição.") String description,
		@NotBlank(message = "Informe a divisão.") String division,
		@NotBlank(message = "Informe o impacto.") String impact,
		Long userId
) {
}
