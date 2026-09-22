package com.fiap.hawk.dto.response;

import com.fiap.hawk.domain.ProjectDocument;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record ProjetoResponse(
		Integer id,
		String name,
		String description,
		String division,
		String stage,
		String status,
		Integer progress,
		Double investment,
		LocalDate deadline,
		Double roi,
		Double financialReturn,
		Double costReduction,
		Double productivity,
		Integer ideaId,
		Integer managerUserId,
		Instant createdAt,
		Instant updatedAt
) {
	public static ProjetoResponse from(ProjectDocument doc) {
		return new ProjetoResponse(
				doc.getPublicId(),
				doc.getName(),
				doc.getDescription(),
				doc.getDivision(),
				doc.getStage() != null ? doc.getStage().getValue() : null,
				doc.getStatus() != null ? doc.getStatus().getValue() : null,
				doc.getProgress(),
				toDouble(doc.getInvestment()),
				doc.getDeadline(),
				toDouble(doc.getRoi()),
				toDouble(doc.getFinancialReturn()),
				toDouble(doc.getCostReduction()),
				toDouble(doc.getProductivity()),
				doc.getIdeaId(),
				doc.getManagerUserId(),
				doc.getCreatedAt(),
				doc.getUpdatedAt()
		);
	}

	private static Double toDouble(BigDecimal value) {
		return value == null ? null : value.doubleValue();
	}
}
