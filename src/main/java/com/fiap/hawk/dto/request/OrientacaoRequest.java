package com.fiap.hawk.dto.request;

import jakarta.validation.constraints.NotBlank;

public record OrientacaoRequest(
		@NotBlank(message = "Informe um título.") String title,
		@NotBlank(message = "Informe uma categoria.") String category,
		@NotBlank(message = "Informe o conteúdo.") String body
) {
}
