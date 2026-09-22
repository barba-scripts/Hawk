package com.fiap.hawk.service;

import com.fiap.hawk.domain.IdeaStatus;
import com.fiap.hawk.domain.ProjectDocument;
import com.fiap.hawk.domain.ProjectStage;
import com.fiap.hawk.domain.ProjectStatus;
import com.fiap.hawk.dto.response.DashboardSummaryResponse;
import com.fiap.hawk.repository.IdeaRepository;
import com.fiap.hawk.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class DashboardService {

	private final ProjectRepository projectRepository;
	private final IdeaRepository ideaRepository;

	public DashboardService(ProjectRepository projectRepository, IdeaRepository ideaRepository) {
		this.projectRepository = projectRepository;
		this.ideaRepository = ideaRepository;
	}

	public DashboardSummaryResponse summary(String division, LocalDate from, LocalDate to) {
		List<ProjectDocument> projects = projectRepository.findAll().stream()
				.filter(p -> division == null || division.isBlank() || division.equalsIgnoreCase(p.getDivision()))
				.filter(p -> from == null || (p.getCreatedAt() != null && !p.getCreatedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate().isBefore(from)))
				.filter(p -> to == null || (p.getCreatedAt() != null && !p.getCreatedAt().atZone(java.time.ZoneOffset.UTC).toLocalDate().isAfter(to)))
				.toList();

		BigDecimal totalInvestment = projects.stream()
				.map(ProjectDocument::getInvestment)
				.filter(v -> v != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		BigDecimal totalReturn = projects.stream()
				.map(ProjectDocument::getFinancialReturn)
				.filter(v -> v != null)
				.reduce(BigDecimal.ZERO, BigDecimal::add);

		List<BigDecimal> rois = projects.stream()
				.map(ProjectDocument::getRoi)
				.filter(v -> v != null)
				.toList();

		List<BigDecimal> costReductions = projects.stream()
				.map(ProjectDocument::getCostReduction)
				.filter(v -> v != null)
				.toList();

		List<BigDecimal> productivities = projects.stream()
				.map(ProjectDocument::getProductivity)
				.filter(v -> v != null)
				.toList();

		long activeProjects = projects.stream().filter(p -> p.getStatus() == ProjectStatus.ATIVO).count();
		long completedProjects = projects.stream().filter(p -> p.getStage() == ProjectStage.CONCLUIDO).count();

		long ideasApproved = ideaRepository.countByStatus(IdeaStatus.APROVADA);
		long ideasInAnalysis = ideaRepository.countByStatus(IdeaStatus.EM_ANALISE);

		long totalProjects = projects.size();

		// Todas as divisões presentes nos projetos filtrados entram no gráfico,
		// não apenas um conjunto fixo (Logística/Passageiros/Comercial).
		Map<String, Long> byDivision = projects.stream()
				.map(ProjectDocument::getDivision)
				.filter(d -> d != null && !d.isBlank())
				.collect(Collectors.groupingBy(d -> d, TreeMap::new, Collectors.counting()));

		List<DashboardSummaryResponse.ProjectsByDivision> projectsByDivision = byDivision.entrySet().stream()
				.sorted(Map.Entry.<String, Long>comparingByValue().reversed()
						.thenComparing(Map.Entry.comparingByKey()))
				.map(e -> new DashboardSummaryResponse.ProjectsByDivision(e.getKey(), e.getValue()))
				.toList();

		return new DashboardSummaryResponse(
				totalInvestment.doubleValue(),
				totalReturn.doubleValue(),
				average(rois),
				average(costReductions),
				average(productivities),
				activeProjects,
				completedProjects,
				ideasApproved,
				ideasInAnalysis,
				totalProjects,
				projectsByDivision
		);
	}

	private double average(List<BigDecimal> values) {
		if (values.isEmpty()) {
			return 0.0;
		}
		BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
		return sum.divide(BigDecimal.valueOf(values.size()), 1, RoundingMode.HALF_UP).doubleValue();
	}
}
