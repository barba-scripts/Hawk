package com.fiap.hawk.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IdeaImpact {
	BAIXO("Baixo"),
	MEDIO("Médio"),
	ALTO("Alto");

	private final String value;

	IdeaImpact(String value) {
		this.value = value;
	}

	@JsonValue
	public String getValue() {
		return value;
	}

	@JsonCreator
	public static IdeaImpact fromValue(String value) {
		if (value == null) {
			throw new IllegalArgumentException("impact is required");
		}
		for (IdeaImpact impact : values()) {
			if (impact.value.equalsIgnoreCase(value.trim())) {
				return impact;
			}
		}
		throw new IllegalArgumentException("Invalid impact: " + value);
	}
}
