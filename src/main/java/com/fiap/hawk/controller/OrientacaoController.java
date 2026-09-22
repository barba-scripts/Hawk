package com.fiap.hawk.controller;

import com.fiap.hawk.dto.request.OrientacaoRequest;
import com.fiap.hawk.dto.response.OrientacaoResponse;
import com.fiap.hawk.security.SecurityUtils;
import com.fiap.hawk.service.StrategyService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/orientacoes")
public class OrientacaoController {

	private final StrategyService strategyService;

	public OrientacaoController(StrategyService strategyService) {
		this.strategyService = strategyService;
	}

	@GetMapping
	public List<OrientacaoResponse> list(
			@RequestParam(required = false) String category,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
		return strategyService.list(category, from, to);
	}

	@GetMapping("/{id}")
	public OrientacaoResponse get(@PathVariable Long id) {
		return strategyService.get(id);
	}

	@PostMapping
	public ResponseEntity<OrientacaoResponse> create(@Valid @RequestBody OrientacaoRequest request) {
		OrientacaoResponse created = strategyService.create(request, SecurityUtils.currentUser());
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	public OrientacaoResponse update(@PathVariable Long id, @Valid @RequestBody OrientacaoRequest request) {
		return strategyService.update(id, request, SecurityUtils.currentUser());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		strategyService.delete(id, SecurityUtils.currentUser());
		return ResponseEntity.noContent().build();
	}
}
