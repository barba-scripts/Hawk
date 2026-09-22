package com.fiap.hawk.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProjectStage {
	PLANEJAMENTO("Planejamento"),
	EXECUCAO("Execução"),
	CONCLUIDO("Concluído"),
	PAUSADO("Pausado");

	private final String value;

	ProjectStage(String value) {
		this.value = value;
	}

	@JsonValue
	public String getValue() {
		return value;
	}

	@JsonCreator
	public static ProjectStage fromValue(String value) {
		if (value == null) {
			throw new IllegalArgumentException("stage is required");
		}
		for (ProjectStage stage : values()) {
			if (stage.value.equalsIgnoreCase(value.trim())) {
				return stage;
			}
		}
		throw new IllegalArgumentException("Invalid project stage: " + value);
	}
}
