package com.fiap.hawk.domain;

import java.time.Instant;

public class ProjectHistoryEntry {

	private String stage;
	private Integer progress;
	private Integer changedByUserId;
	private Instant at;
	private String observation;

	public ProjectHistoryEntry() {
	}

	public ProjectHistoryEntry(String stage, Integer progress, Integer changedByUserId, Instant at, String observation) {
		this.stage = stage;
		this.progress = progress;
		this.changedByUserId = changedByUserId;
		this.at = at;
		this.observation = observation;
	}

	public String getStage() {
		return stage;
	}

	public void setStage(String stage) {
		this.stage = stage;
	}

	public Integer getProgress() {
		return progress;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
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
