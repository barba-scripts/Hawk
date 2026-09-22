package com.fiap.hawk.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fiap.hawk.domain.UserDocument;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserResponse(
		Integer id,
		String name,
		String email,
		String role,
		String division,
		String initials
) {
	public static UserResponse from(UserDocument user) {
		return new UserResponse(
				user.getPublicId(),
				user.getName(),
				user.getEmail(),
				user.getRole().getValue(),
				user.getDivision(),
				user.initials()
		);
	}

	public static UserResponse fromLogin(UserDocument user) {
		return new UserResponse(
				user.getPublicId(),
				user.getName(),
				null,
				user.getRole().getValue(),
				user.getDivision(),
				user.initials()
		);
	}
}
