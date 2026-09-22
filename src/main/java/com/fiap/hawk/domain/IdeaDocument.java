package com.fiap.hawk.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "ideas")
public class IdeaDocument {

	@Id
	private String id;
	private Long publicId;
	private String title;
	private String description;
	private String division;
	private IdeaImpact impact;
	private IdeaStatus status;
	@Indexed
	private Long authorUserId;
	@Indexed
	private Long strategyId;
	private Integer score;
	private String observation;
	private Long reviewedByUserId;
	private Instant reviewedAt;
	private LocalDate date;
	private Instant createdAt;
	private Instant updatedAt;
	private List<StatusHistoryEntry> history = new ArrayList<>();

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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDivision() {
		return division;
	}

	public void setDivision(String division) {
		this.division = division;
	}

	public IdeaImpact getImpact() {
		return impact;
	}

	public void setImpact(IdeaImpact impact) {
		this.impact = impact;
	}

	public IdeaStatus getStatus() {
		return status;
	}

	public void setStatus(IdeaStatus status) {
		this.status = status;
	}

	public Long getAuthorUserId() {
		return authorUserId;
	}

	public void setAuthorUserId(Long authorUserId) {
		this.authorUserId = authorUserId;
	}

	public Long getStrategyId() {
		return strategyId;
	}

	public void setStrategyId(Long strategyId) {
		this.strategyId = strategyId;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public String getObservation() {
		return observation;
	}

	public void setObservation(String observation) {
		this.observation = observation;
	}

	public Long getReviewedByUserId() {
		return reviewedByUserId;
	}

	public void setReviewedByUserId(Long reviewedByUserId) {
		this.reviewedByUserId = reviewedByUserId;
	}

	public Instant getReviewedAt() {
		return reviewedAt;
	}

	public void setReviewedAt(Instant reviewedAt) {
		this.reviewedAt = reviewedAt;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
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

	public List<StatusHistoryEntry> getHistory() {
		return history;
	}

	public void setHistory(List<StatusHistoryEntry> history) {
		this.history = history;
	}
}
