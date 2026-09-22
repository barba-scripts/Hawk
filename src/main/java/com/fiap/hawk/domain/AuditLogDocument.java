package com.fiap.hawk.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "audit_logs")
public class AuditLogDocument {

	@Id
	private String id;
	private Long actorUserId;
	private String action;
	private String entityType;
	private Long entityId;
	private Map<String, Object> before;
	private Map<String, Object> after;
	private Instant at;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getActorUserId() {
		return actorUserId;
	}

	public void setActorUserId(Long actorUserId) {
		this.actorUserId = actorUserId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getEntityType() {
		return entityType;
	}

	public void setEntityType(String entityType) {
		this.entityType = entityType;
	}

	public Long getEntityId() {
		return entityId;
	}

	public void setEntityId(Long entityId) {
		this.entityId = entityId;
	}

	public Map<String, Object> getBefore() {
		return before;
	}

	public void setBefore(Map<String, Object> before) {
		this.before = before;
	}

	public Map<String, Object> getAfter() {
		return after;
	}

	public void setAfter(Map<String, Object> after) {
		this.after = after;
	}

	public Instant getAt() {
		return at;
	}

	public void setAt(Instant at) {
		this.at = at;
	}
}
