package com.fiap.hawk.service;

import com.fiap.hawk.domain.StrategyDocument;
import com.fiap.hawk.dto.request.OrientacaoRequest;
import com.fiap.hawk.dto.response.OrientacaoResponse;
import com.fiap.hawk.exception.ApiException;
import com.fiap.hawk.repository.StrategyRepository;
import com.fiap.hawk.security.UserPrincipal;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StrategyService {

	private final StrategyRepository strategyRepository;
	private final SequenceService sequenceService;
	private final AuditService auditService;

	public StrategyService(StrategyRepository strategyRepository, SequenceService sequenceService,
			AuditService auditService) {
		this.strategyRepository = strategyRepository;
		this.sequenceService = sequenceService;
		this.auditService = auditService;
	}

	public List<OrientacaoResponse> list(String category, LocalDate from, LocalDate to) {
		return strategyRepository.findByActiveTrueOrderByCreatedAtDesc().stream()
				.filter(s -> category == null || category.isBlank() || category.equalsIgnoreCase(s.getCategory()))
				.filter(s -> from == null || (s.getDate() != null && !s.getDate().isBefore(from)))
				.filter(s -> to == null || (s.getDate() != null && !s.getDate().isAfter(to)))
				.map(OrientacaoResponse::from)
				.toList();
	}

	public OrientacaoResponse get(Integer id) {
		return OrientacaoResponse.from(require(id));
	}

	public OrientacaoResponse create(OrientacaoRequest request, UserPrincipal principal) {
		Instant now = Instant.now();
		StrategyDocument doc = new StrategyDocument();
		doc.setPublicId(sequenceService.next("strategies"));
		doc.setTitle(request.title());
		doc.setCategory(request.category());
		doc.setBody(request.body());
		doc.setActive(true);
		doc.setCreatedByUserId(principal.getPublicId());
		doc.setDate(LocalDate.now());
		doc.setCreatedAt(now);
		doc.setUpdatedAt(now);
		StrategyDocument saved = strategyRepository.save(doc);

		auditService.log(principal.getPublicId(), "CREATE", "strategy", saved.getPublicId(), null, snapshot(saved));
		return OrientacaoResponse.from(saved);
	}

	public OrientacaoResponse update(Integer id, OrientacaoRequest request, UserPrincipal principal) {
		StrategyDocument doc = require(id);
		Map<String, Object> before = snapshot(doc);
		doc.setTitle(request.title());
		doc.setCategory(request.category());
		doc.setBody(request.body());
		doc.setUpdatedAt(Instant.now());
		StrategyDocument saved = strategyRepository.save(doc);
		auditService.log(principal.getPublicId(), "UPDATE", "strategy", saved.getPublicId(), before, snapshot(saved));
		return OrientacaoResponse.from(saved);
	}

	public void delete(Integer id, UserPrincipal principal) {
		StrategyDocument doc = require(id);
		Map<String, Object> before = snapshot(doc);
		doc.setActive(false);
		doc.setUpdatedAt(Instant.now());
		strategyRepository.save(doc);
		auditService.log(principal.getPublicId(), "DELETE", "strategy", doc.getPublicId(), before, snapshot(doc));
	}

	public StrategyDocument findCurrentActive() {
		return strategyRepository.findFirstByActiveTrueOrderByCreatedAtDesc().orElse(null);
	}

	private StrategyDocument require(Integer id) {
		return strategyRepository.findByPublicId(id)
				.filter(StrategyDocument::isActive)
				.orElseThrow(() -> ApiException.notFound("Orientação não encontrada."));
	}

	private Map<String, Object> snapshot(StrategyDocument doc) {
		Map<String, Object> map = new HashMap<>();
		map.put("title", doc.getTitle());
		map.put("category", doc.getCategory());
		map.put("body", doc.getBody());
		map.put("active", doc.isActive());
		return map;
	}
}
