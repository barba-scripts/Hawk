package com.fiap.hawk.service;

import com.fiap.hawk.domain.IdeaDocument;
import com.fiap.hawk.domain.IdeaImpact;
import com.fiap.hawk.domain.IdeaStatus;
import com.fiap.hawk.domain.Role;
import com.fiap.hawk.domain.StatusHistoryEntry;
import com.fiap.hawk.domain.StrategyDocument;
import com.fiap.hawk.dto.request.CreateIdeiaRequest;
import com.fiap.hawk.dto.request.ReviewIdeiaRequest;
import com.fiap.hawk.dto.response.IdeiaResponse;
import com.fiap.hawk.exception.ApiException;
import com.fiap.hawk.repository.IdeaRepository;
import com.fiap.hawk.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class IdeaService {

	private final IdeaRepository ideaRepository;
	private final SequenceService sequenceService;
	private final StrategyService strategyService;
	private final AuditService auditService;

	public IdeaService(IdeaRepository ideaRepository, SequenceService sequenceService,
			StrategyService strategyService, AuditService auditService) {
		this.ideaRepository = ideaRepository;
		this.sequenceService = sequenceService;
		this.strategyService = strategyService;
		this.auditService = auditService;
	}

	public List<IdeiaResponse> list(UserPrincipal principal, String status, String division,
			String userId, Boolean mine, String sort) {
		List<IdeaDocument> ideas = ideaRepository.findAllByOrderByCreatedAtDesc();

		boolean mineOnly = principal.getRole() == Role.OPERADOR
				|| Boolean.TRUE.equals(mine)
				|| (userId != null && "me".equalsIgnoreCase(userId));

		if (mineOnly) {
			ideas = ideas.stream()
					.filter(idea -> idea.getAuthorUserId().equals(principal.getPublicId()))
					.toList();
		} else if (userId != null && !userId.isBlank()) {
			Long parsedUserId = Long.valueOf(userId);
			ideas = ideas.stream()
					.filter(idea -> idea.getAuthorUserId().equals(parsedUserId))
					.toList();
		}

		IdeaStatus statusFilter = null;
		if (status != null && !status.isBlank()) {
			statusFilter = IdeaStatus.fromValue(status);
		}
		IdeaStatus finalStatus = statusFilter;

		List<IdeaDocument> filtered = ideas.stream()
				.filter(idea -> finalStatus == null || idea.getStatus() == finalStatus)
				.filter(idea -> division == null || division.isBlank() || division.equalsIgnoreCase(idea.getDivision()))
				.sorted(resolveComparator(sort))
				.toList();

		return filtered.stream().map(IdeiaResponse::from).toList();
	}

	public IdeiaResponse get(Long id, UserPrincipal principal) {
		IdeaDocument idea = require(id);
		assertCanView(idea, principal);
		return IdeiaResponse.from(idea);
	}

	public IdeiaResponse create(CreateIdeiaRequest request, UserPrincipal principal) {
		if (principal.getRole() != Role.OPERADOR) {
			throw ApiException.forbidden("Apenas operadores podem cadastrar ideias.");
		}

		IdeaImpact impact;
		try {
			impact = IdeaImpact.fromValue(request.impact());
		} catch (IllegalArgumentException ex) {
			throw ApiException.badRequest(ex.getMessage());
		}

		Instant now = Instant.now();
		IdeaDocument doc = new IdeaDocument();
		doc.setPublicId(sequenceService.next("ideas"));
		doc.setTitle(request.title());
		doc.setDescription(request.description());
		doc.setDivision(request.division());
		doc.setImpact(impact);
		doc.setStatus(IdeaStatus.ENVIADA);
		doc.setAuthorUserId(principal.getPublicId());
		doc.setDate(LocalDate.now());
		doc.setCreatedAt(now);
		doc.setUpdatedAt(now);

		StrategyDocument current = strategyService.findCurrentActive();
		if (current != null) {
			doc.setStrategyId(current.getPublicId());
		}

		List<StatusHistoryEntry> history = new ArrayList<>();
		history.add(new StatusHistoryEntry(IdeaStatus.ENVIADA.getValue(), principal.getPublicId(), now, null));
		doc.setHistory(history);

		IdeaDocument saved = ideaRepository.save(doc);
		auditService.log(principal.getPublicId(), "CREATE", "idea", saved.getPublicId(), null, snapshot(saved));
		return IdeiaResponse.from(saved);
	}

	public IdeiaResponse review(Long id, ReviewIdeiaRequest request, UserPrincipal principal) {
		if (principal.getRole() != Role.GESTOR) {
			throw ApiException.forbidden("Apenas gestores podem avaliar ideias.");
		}

		IdeaDocument idea = require(id);
		IdeaStatus nextStatus;
		try {
			nextStatus = IdeaStatus.fromValue(request.status());
		} catch (IllegalArgumentException ex) {
			throw ApiException.badRequest(ex.getMessage());
		}

		if (!idea.getStatus().canTransitionTo(nextStatus)) {
			// Permite Enviada → Aprovada/Recusada registrando Em análise automaticamente (fluxo do app).
			if (idea.getStatus() == IdeaStatus.ENVIADA
					&& (nextStatus == IdeaStatus.APROVADA || nextStatus == IdeaStatus.RECUSADA)) {
				idea.getHistory().add(new StatusHistoryEntry(
						IdeaStatus.EM_ANALISE.getValue(),
						principal.getPublicId(),
						Instant.now(),
						"Encaminhada para decisão"
				));
				idea.setStatus(IdeaStatus.EM_ANALISE);
			} else {
				throw ApiException.conflict("Transição inválida de " + idea.getStatus().getValue()
						+ " para " + nextStatus.getValue() + ".");
			}
		}

		if (request.score() != null && (request.score() < 0 || request.score() > 100)) {
			throw ApiException.badRequest("score deve estar entre 0 e 100.");
		}

		Map<String, Object> before = snapshot(idea);
		Instant now = Instant.now();
		idea.setStatus(nextStatus);
		if (request.score() != null) {
			idea.setScore(request.score());
		}
		if (request.observation() != null) {
			idea.setObservation(request.observation());
		}
		idea.setReviewedByUserId(principal.getPublicId());
		idea.setReviewedAt(now);
		idea.setUpdatedAt(now);
		idea.getHistory().add(new StatusHistoryEntry(
				nextStatus.getValue(),
				principal.getPublicId(),
				now,
				request.observation()
		));

		IdeaDocument saved = ideaRepository.save(idea);
		auditService.log(principal.getPublicId(), "REVIEW", "idea", saved.getPublicId(), before, snapshot(saved));
		return IdeiaResponse.from(saved);
	}

	public IdeaDocument require(Long id) {
		return ideaRepository.findByPublicId(id)
				.orElseThrow(() -> ApiException.notFound("Ideia não encontrada."));
	}

	private void assertCanView(IdeaDocument idea, UserPrincipal principal) {
		if (principal.getRole() == Role.OPERADOR && !idea.getAuthorUserId().equals(principal.getPublicId())) {
			throw ApiException.forbidden("Operadores só consultam as próprias ideias.");
		}
	}

	private Comparator<IdeaDocument> resolveComparator(String sort) {
		if (sort == null || sort.isBlank()) {
			return Comparator.comparing(IdeaDocument::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder()));
		}
		boolean desc = sort.startsWith("-");
		String field = desc ? sort.substring(1) : sort;
		Comparator<IdeaDocument> comparator = switch (field) {
			case "score" -> Comparator.comparing(IdeaDocument::getScore, Comparator.nullsLast(Integer::compareTo));
			case "date", "createdAt" -> Comparator.comparing(IdeaDocument::getCreatedAt, Comparator.nullsLast(Instant::compareTo));
			default -> Comparator.comparing(IdeaDocument::getCreatedAt, Comparator.nullsLast(Instant::compareTo));
		};
		return desc ? comparator.reversed() : comparator;
	}

	private Map<String, Object> snapshot(IdeaDocument doc) {
		Map<String, Object> map = new HashMap<>();
		map.put("status", doc.getStatus() != null ? doc.getStatus().getValue() : null);
		map.put("score", doc.getScore());
		map.put("observation", doc.getObservation());
		map.put("strategyId", doc.getStrategyId());
		return map;
	}
}
