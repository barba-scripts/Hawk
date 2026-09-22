package com.fiap.hawk.service;

import com.fiap.hawk.domain.IdeaDocument;
import com.fiap.hawk.domain.IdeaStatus;
import com.fiap.hawk.domain.ProjectDocument;
import com.fiap.hawk.domain.ProjectHistoryEntry;
import com.fiap.hawk.domain.ProjectStage;
import com.fiap.hawk.domain.ProjectStatus;
import com.fiap.hawk.domain.StrategyDocument;
import com.fiap.hawk.dto.request.CreateProjetoRequest;
import com.fiap.hawk.dto.request.UpdateProjetoRequest;
import com.fiap.hawk.dto.response.ProjetoResponse;
import com.fiap.hawk.exception.ApiException;
import com.fiap.hawk.repository.ProjectRepository;
import com.fiap.hawk.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProjectService {

	private final ProjectRepository projectRepository;
	private final IdeaService ideaService;
	private final StrategyService strategyService;
	private final SequenceService sequenceService;
	private final AuditService auditService;

	public ProjectService(ProjectRepository projectRepository, IdeaService ideaService,
			StrategyService strategyService, SequenceService sequenceService, AuditService auditService) {
		this.projectRepository = projectRepository;
		this.ideaService = ideaService;
		this.strategyService = strategyService;
		this.sequenceService = sequenceService;
		this.auditService = auditService;
	}

	public List<ProjetoResponse> list(String division, String stage, String status, String sort) {
		ProjectStage stageFilter = parseStage(stage);
		ProjectStatus statusFilter = parseStatus(status);

		return projectRepository.findAllByOrderByCreatedAtDesc().stream()
				.filter(p -> division == null || division.isBlank() || division.equalsIgnoreCase(p.getDivision()))
				.filter(p -> stageFilter == null || p.getStage() == stageFilter)
				.filter(p -> statusFilter == null || p.getStatus() == statusFilter)
				.sorted(resolveComparator(sort))
				.map(ProjetoResponse::from)
				.toList();
	}

	public ProjetoResponse get(Integer id) {
		return ProjetoResponse.from(require(id));
	}

	public ProjetoResponse create(CreateProjetoRequest request, UserPrincipal principal) {
		IdeaDocument idea = ideaService.require(request.ideaId());
		if (idea.getStatus() != IdeaStatus.APROVADA) {
			throw ApiException.conflict("Só é possível criar projeto a partir de ideia Aprovada.");
		}
		if (projectRepository.existsByIdeaId(request.ideaId())) {
			throw ApiException.conflict("Esta ideia já originou um projeto.");
		}

		ProjectStage stage = parseStageRequired(request.stage());
		ProjectStatus status = parseStatusRequired(request.status());

		Instant now = Instant.now();
		ProjectDocument doc = new ProjectDocument();
		doc.setPublicId(sequenceService.next("projects"));
		doc.setName(request.name());
		doc.setDescription(request.description());
		doc.setDivision(request.division());
		doc.setStage(stage);
		doc.setStatus(status);
		doc.setProgress(defaultProgress(stage));
		doc.setInvestment(request.investment());
		doc.setDeadline(request.deadline());
		doc.setIdeaId(request.ideaId());
		doc.setManagerUserId(principal.getPublicId());
		doc.setCreatedAt(now);
		doc.setUpdatedAt(now);
		doc.setRoi(calculateRoi(request.investment(), null));

		StrategyDocument current = strategyService.findCurrentActive();
		if (current != null) {
			doc.setStrategyId(current.getPublicId());
		} else if (idea.getStrategyId() != null) {
			doc.setStrategyId(idea.getStrategyId());
		}

		List<ProjectHistoryEntry> history = new ArrayList<>();
		history.add(new ProjectHistoryEntry(stage.getValue(), doc.getProgress(), principal.getPublicId(), now, "Criado"));
		doc.setHistory(history);

		ProjectDocument saved = projectRepository.save(doc);
		auditService.log(principal.getPublicId(), "CREATE", "project", saved.getPublicId(), null, snapshot(saved));
		return ProjetoResponse.from(saved);
	}

	public ProjetoResponse update(Integer id, UpdateProjetoRequest request, UserPrincipal principal) {
		ProjectDocument doc = require(id);
		Map<String, Object> before = snapshot(doc);
		Instant now = Instant.now();

		if (request.name() != null) {
			doc.setName(request.name());
		}
		if (request.description() != null) {
			doc.setDescription(request.description());
		}
		if (request.division() != null) {
			doc.setDivision(request.division());
		}
		if (request.stage() != null) {
			doc.setStage(parseStageRequired(request.stage()));
		}
		if (request.status() != null) {
			doc.setStatus(parseStatusRequired(request.status()));
		}
		if (request.progress() != null) {
			doc.setProgress(request.progress());
		} else if (request.stage() != null && doc.getStage() == ProjectStage.CONCLUIDO) {
			doc.setProgress(100);
		}
		if (request.investment() != null) {
			doc.setInvestment(request.investment());
		}
		if (request.deadline() != null) {
			doc.setDeadline(request.deadline());
		}
		if (request.financialReturn() != null) {
			doc.setFinancialReturn(request.financialReturn());
		}
		if (request.costReduction() != null) {
			doc.setCostReduction(request.costReduction());
		}
		if (request.productivity() != null) {
			doc.setProductivity(request.productivity());
		}

		doc.setRoi(calculateRoi(doc.getInvestment(), doc.getFinancialReturn()));
		doc.setUpdatedAt(now);
		doc.getHistory().add(new ProjectHistoryEntry(
				doc.getStage().getValue(),
				doc.getProgress(),
				principal.getPublicId(),
				now,
				request.observation()
		));

		ProjectDocument saved = projectRepository.save(doc);
		auditService.log(principal.getPublicId(), "UPDATE", "project", saved.getPublicId(), before, snapshot(saved));
		return ProjetoResponse.from(saved);
	}

	public List<ProjectDocument> findAll() {
		return projectRepository.findAll();
	}

	/**
	 * ROI = ((financialReturn - investment) / investment) * 100
	 */
	public static BigDecimal calculateRoi(BigDecimal investment, BigDecimal financialReturn) {
		if (investment == null || investment.compareTo(BigDecimal.ZERO) == 0 || financialReturn == null) {
			return null;
		}
		return financialReturn.subtract(investment)
				.divide(investment, 6, RoundingMode.HALF_UP)
				.multiply(BigDecimal.valueOf(100))
				.setScale(1, RoundingMode.HALF_UP);
	}

	private ProjectDocument require(Integer id) {
		return projectRepository.findByPublicId(id)
				.orElseThrow(() -> ApiException.notFound("Projeto não encontrado."));
	}

	private int defaultProgress(ProjectStage stage) {
		return switch (stage) {
			case PLANEJAMENTO -> 0;
			case EXECUCAO -> 50;
			case CONCLUIDO -> 100;
			case PAUSADO -> 0;
		};
	}

	private ProjectStage parseStage(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return ProjectStage.fromValue(value);
		} catch (IllegalArgumentException ex) {
			throw ApiException.badRequest(ex.getMessage());
		}
	}

	private ProjectStage parseStageRequired(String value) {
		ProjectStage stage = parseStage(value);
		if (stage == null) {
			throw ApiException.badRequest("stage é obrigatório.");
		}
		return stage;
	}

	private ProjectStatus parseStatus(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return ProjectStatus.fromValue(value);
		} catch (IllegalArgumentException ex) {
			throw ApiException.badRequest(ex.getMessage());
		}
	}

	private ProjectStatus parseStatusRequired(String value) {
		ProjectStatus status = parseStatus(value);
		if (status == null) {
			throw ApiException.badRequest("status é obrigatório.");
		}
		return status;
	}

	private Comparator<ProjectDocument> resolveComparator(String sort) {
		if (sort == null || sort.isBlank()) {
			return Comparator.comparing(ProjectDocument::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
		}
		boolean desc = sort.startsWith("-");
		String field = desc ? sort.substring(1) : sort;
		Comparator<ProjectDocument> comparator = switch (field) {
			case "roi" -> Comparator.comparing(ProjectDocument::getRoi, Comparator.nullsLast(BigDecimal::compareTo));
			case "investment" -> Comparator.comparing(ProjectDocument::getInvestment, Comparator.nullsLast(BigDecimal::compareTo));
			case "deadline" -> Comparator.comparing(ProjectDocument::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()));
			default -> Comparator.comparing(ProjectDocument::getCreatedAt, Comparator.nullsLast(Instant::compareTo));
		};
		return desc ? comparator.reversed() : comparator;
	}

	private Map<String, Object> snapshot(ProjectDocument doc) {
		Map<String, Object> map = new HashMap<>();
		map.put("stage", doc.getStage() != null ? doc.getStage().getValue() : null);
		map.put("status", doc.getStatus() != null ? doc.getStatus().getValue() : null);
		map.put("progress", doc.getProgress());
		map.put("investment", doc.getInvestment());
		map.put("financialReturn", doc.getFinancialReturn());
		map.put("roi", doc.getRoi());
		return map;
	}
}
