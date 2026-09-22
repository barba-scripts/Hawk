package com.fiap.hawk.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Role {
	OPERADOR("operador"),
	GESTOR("gestor"),
	LIDER("lider");

	private final String value;

	Role(String value) {
		this.value = value;
	}

	@JsonValue
	public String getValue() {
		return value;
	}

	public String authority() {
		return "ROLE_" + name();
	}

	@JsonCreator
	public static Role fromValue(String value) {
		if (value == null) {
			throw new IllegalArgumentException("role is required");
		}
		String normalized = value.trim().toLowerCase();
		for (Role role : values()) {
			if (role.value.equals(normalized) || role.name().equalsIgnoreCase(normalized)) {
				return role;
			}
		}
		throw new IllegalArgumentException("Invalid role: " + value);
	}
}
