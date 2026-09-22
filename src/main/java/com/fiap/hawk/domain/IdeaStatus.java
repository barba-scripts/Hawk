package com.fiap.hawk.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum IdeaStatus {
	ENVIADA("Enviada"),
	EM_ANALISE("Em análise"),
	APROVADA("Aprovada"),
	RECUSADA("Recusada");

	private final String value;

	IdeaStatus(String value) {
		this.value = value;
	}

	@JsonValue
	public String getValue() {
		return value;
	}

	@JsonCreator
	public static IdeaStatus fromValue(String value) {
		if (value == null) {
			throw new IllegalArgumentException("status is required");
		}
		for (IdeaStatus status : values()) {
			if (status.value.equalsIgnoreCase(value.trim())) {
				return status;
			}
		}
		throw new IllegalArgumentException("Invalid idea status: " + value);
	}

	public boolean canTransitionTo(IdeaStatus next) {
		return switch (this) {
			case ENVIADA -> next == EM_ANALISE;
			case EM_ANALISE -> next == APROVADA || next == RECUSADA;
			case APROVADA, RECUSADA -> false;
		};
	}
}
