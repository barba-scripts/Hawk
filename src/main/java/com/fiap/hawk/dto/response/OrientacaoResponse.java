package com.fiap.hawk.dto.response;

import com.fiap.hawk.domain.StrategyDocument;

import java.time.Instant;
import java.time.LocalDate;

public record OrientacaoResponse(
		Long id,
		String title,
		String category,
		LocalDate date,
		String body,
		Long createdByUserId,
		Instant createdAt,
		Instant updatedAt
) {
	public static OrientacaoResponse from(StrategyDocument doc) {
		return new OrientacaoResponse(
				doc.getPublicId(),
				doc.getTitle(),
				doc.getCategory(),
				doc.getDate(),
				doc.getBody(),
				doc.getCreatedByUserId(),
				doc.getCreatedAt(),
				doc.getUpdatedAt()
		);
	}
}
