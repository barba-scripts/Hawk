package com.fiap.hawk.dto.response;

import com.fiap.hawk.domain.IdeaDocument;

import java.time.Instant;
import java.time.LocalDate;

public record IdeiaResponse(
		Long id,
		String title,
		String description,
		String division,
		String impact,
		String status,
		Long userId,
		Integer score,
		LocalDate date,
		String observation,
		Long strategyId,
		Long reviewedByUserId,
		Instant reviewedAt
) {
	public static IdeiaResponse from(IdeaDocument doc) {
		return new IdeiaResponse(
				doc.getPublicId(),
				doc.getTitle(),
				doc.getDescription(),
				doc.getDivision(),
				doc.getImpact() != null ? doc.getImpact().getValue() : null,
				doc.getStatus() != null ? doc.getStatus().getValue() : null,
				doc.getAuthorUserId(),
				doc.getScore(),
				doc.getDate(),
				doc.getObservation(),
				doc.getStrategyId(),
				doc.getReviewedByUserId(),
				doc.getReviewedAt()
		);
	}
}
