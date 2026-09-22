package com.fiap.hawk.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStatus {
	ATIVO("Ativo"),
	INATIVO("Inativo");

	private final String value;

	ProjectStatus(String value) {
		this.value = value;
	}

	@JsonValue
	public String getValue() {
		return value;
	}

	@JsonCreator
	public static ProjectStatus fromValue(String value) {
		if (value == null) {
			throw new IllegalArgumentException("status is required");
		}
		for (ProjectStatus status : values()) {
			if (status.value.equalsIgnoreCase(value.trim())) {
				return status;
			}
		}
		throw new IllegalArgumentException("Invalid project status: " + value);
	}
}
