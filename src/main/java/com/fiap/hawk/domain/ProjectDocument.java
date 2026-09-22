package com.fiap.hawk.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "projects")
public class ProjectDocument {

	@Id
	private String id;
	private Long publicId;
	private String name;
	private String description;
	private String division;
	private ProjectStage stage;
	private ProjectStatus status;
	private Integer progress;
	private BigDecimal investment;
	private LocalDate deadline;
	private BigDecimal financialReturn;
	private BigDecimal roi;
	private BigDecimal costReduction;
	private BigDecimal productivity;
	@Indexed(unique = true, sparse = true)
	private Long ideaId;
	@Indexed
	private Long managerUserId;
	private Long strategyId;
	private Instant createdAt;
	private Instant updatedAt;
	private List<ProjectHistoryEntry> history = new ArrayList<>();

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

	public ProjectStage getStage() {
		return stage;
	}

	public void setStage(ProjectStage stage) {
		this.stage = stage;
	}

	public ProjectStatus getStatus() {
		return status;
	}

	public void setStatus(ProjectStatus status) {
		this.status = status;
	}

	public Integer getProgress() {
		return progress;
	}

	public void setProgress(Integer progress) {
		this.progress = progress;
	}

	public BigDecimal getInvestment() {
		return investment;
	}

	public void setInvestment(BigDecimal investment) {
		this.investment = investment;
	}

	public LocalDate getDeadline() {
		return deadline;
	}

	public void setDeadline(LocalDate deadline) {
		this.deadline = deadline;
	}

	public BigDecimal getFinancialReturn() {
		return financialReturn;
	}

	public void setFinancialReturn(BigDecimal financialReturn) {
		this.financialReturn = financialReturn;
	}

	public BigDecimal getRoi() {
		return roi;
	}

	public void setRoi(BigDecimal roi) {
		this.roi = roi;
	}

	public BigDecimal getCostReduction() {
		return costReduction;
	}

	public void setCostReduction(BigDecimal costReduction) {
		this.costReduction = costReduction;
	}

	public BigDecimal getProductivity() {
		return productivity;
	}

	public void setProductivity(BigDecimal productivity) {
		this.productivity = productivity;
	}

	public Long getIdeaId() {
		return ideaId;
	}

	public void setIdeaId(Long ideaId) {
		this.ideaId = ideaId;
	}

	public Long getManagerUserId() {
		return managerUserId;
	}

	public void setManagerUserId(Long managerUserId) {
		this.managerUserId = managerUserId;
	}

	public Long getStrategyId() {
		return strategyId;
	}

	public void setStrategyId(Long strategyId) {
		this.strategyId = strategyId;
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

	public List<ProjectHistoryEntry> getHistory() {
		return history;
	}

	public void setHistory(List<ProjectHistoryEntry> history) {
		this.history = history;
	}
}
