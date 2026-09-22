package com.fiap.hawk.domain;

import java.time.Instant;

public class StatusHistoryEntry {

	private String status;
	private Integer changedByUserId;
	private Instant at;
	private String observation;

	public StatusHistoryEntry() {
	}

	public StatusHistoryEntry(String status, Integer changedByUserId, Instant at, String observation) {
		this.status = status;
		this.changedByUserId = changedByUserId;
		this.at = at;
		this.observation = observation;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Integer getChangedByUserId() {
		return changedByUserId;
	}

	public void setChangedByUserId(Integer changedByUserId) {
		this.changedByUserId = changedByUserId;
	}

	public Instant getAt() {
		return at;
	}

	public void setAt(Instant at) {
		this.at = at;
	}

	public String getObservation() {
		return observation;
	}

	public void setObservation(String observation) {
		this.observation = observation;
	}
}
