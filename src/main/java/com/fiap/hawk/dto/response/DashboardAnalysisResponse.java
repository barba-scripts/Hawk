package com.fiap.hawk.dto.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record DashboardAnalysisResponse(
		@NotBlank @Size(max = 4000) String summary,
		@NotNull @Size(max = 5) List<@NotNull @Valid Observation> highlights,
		@NotNull @Size(max = 5) List<@NotNull @Valid AttentionPoint> attentionPoints,
		@NotNull @Size(max = 5) List<@NotNull @Valid Observation> trends,
		@NotNull @Size(max = 5) List<@NotNull @Valid Recommendation> recommendations
) {

	public record Observation(
			@NotBlank @Size(max = 200) String title,
			@NotBlank @Size(max = 2000) String description
	) {
	}

	public record AttentionPoint(
			@NotBlank @Size(max = 200) String title,
			@NotBlank @Size(max = 2000) String description,
			@NotNull @Pattern(regexp = "low|medium|high") String severity
	) {
	}

	public record Recommendation(
			@NotBlank @Size(max = 200) String title,
			@NotBlank @Size(max = 2000) String description,
			@NotNull @Pattern(regexp = "low|medium|high") String priority
	) {
	}
}
