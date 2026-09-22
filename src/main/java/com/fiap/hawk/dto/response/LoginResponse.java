package com.fiap.hawk.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record LoginResponse(
		String token,
		Long expiresIn,
		String refreshToken,
		UserResponse user
) {
}
