package com.fiap.hawk.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "users")
public class UserDocument {

	@Id
	private String id;

	@Indexed(unique = true)
	private Long publicId;

	private String name;

	@Indexed(unique = true)
	private String email;

	private String passwordHash;
	private Role role;
	private String division;
	private boolean active = true;
	private Instant createdAt;
	private Instant updatedAt;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getPublicId() {
		return publicId;
	}

	public void setPublicId(Long publicId) {
		this.publicId = publicId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getDivision() {
		return division;
	}

	public void setDivision(String division) {
		this.division = division;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String initials() {
		if (name == null || name.isBlank()) {
			return "";
		}
		String[] parts = name.trim().split("\\s+");
		if (parts.length == 1) {
			return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
		}
		return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
	}
}
