package com.fiap.hawk.controller;

import com.fiap.hawk.dto.request.CreateProjetoRequest;
import com.fiap.hawk.dto.request.UpdateProjetoRequest;
import com.fiap.hawk.dto.response.ProjetoResponse;
import com.fiap.hawk.security.SecurityUtils;
import com.fiap.hawk.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/projetos")
public class ProjetoController {

	private final ProjectService projectService;

	public ProjetoController(ProjectService projectService) {
		this.projectService = projectService;
	}

	@GetMapping
	public List<ProjetoResponse> list(
			@RequestParam(required = false) String division,
			@RequestParam(required = false) String stage,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String sort) {
		return projectService.list(division, stage, status, sort);
	}

	@GetMapping("/{id}")
	public ProjetoResponse get(@PathVariable Long id) {
		return projectService.get(id);
	}

	@PostMapping
	public ResponseEntity<ProjetoResponse> create(@Valid @RequestBody CreateProjetoRequest request) {
		ProjetoResponse created = projectService.create(request, SecurityUtils.currentUser());
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PatchMapping("/{id}")
	public ProjetoResponse update(@PathVariable Long id, @Valid @RequestBody UpdateProjetoRequest request) {
		return projectService.update(id, request, SecurityUtils.currentUser());
	}
}
