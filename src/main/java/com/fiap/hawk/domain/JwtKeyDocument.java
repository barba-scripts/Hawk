package com.fiap.hawk.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Chaves JWT no banco de admin, conforme requisito do projeto.
 */
@Document(collection = "jwt_keys")
public class JwtKeyDocument {

	public static final String ACTIVE_KEY_ID = "active";

	@Id
	private String id;
	private String secret;
	private Instant createdAt;
	private Instant rotatedAt;
	private boolean active = true;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getRotatedAt() {
		return rotatedAt;
	}

	public void setRotatedAt(Instant rotatedAt) {
		this.rotatedAt = rotatedAt;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
