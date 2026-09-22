package com.fiap.hawk.controller;

import com.fiap.hawk.dto.request.CreateIdeiaRequest;
import com.fiap.hawk.dto.request.ReviewIdeiaRequest;
import com.fiap.hawk.dto.response.IdeiaResponse;
import com.fiap.hawk.security.SecurityUtils;
import com.fiap.hawk.service.IdeaService;
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
@RequestMapping("/api/v1/ideias")
public class IdeiaController {

	private final IdeaService ideaService;

	public IdeiaController(IdeaService ideaService) {
		this.ideaService = ideaService;
	}

	@GetMapping
	public List<IdeiaResponse> list(
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String division,
			@RequestParam(required = false) String userId,
			@RequestParam(required = false) Boolean mine,
			@RequestParam(required = false) String sort) {
		return ideaService.list(SecurityUtils.currentUser(), status, division, userId, mine, sort);
	}

	@GetMapping("/{id}")
	public IdeiaResponse get(@PathVariable Integer id) {
		return ideaService.get(id, SecurityUtils.currentUser());
	}

	@PostMapping
	public ResponseEntity<IdeiaResponse> create(@Valid @RequestBody CreateIdeiaRequest request) {
		IdeiaResponse created = ideaService.create(request, SecurityUtils.currentUser());
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PatchMapping("/{id}")
	public IdeiaResponse review(@PathVariable Integer id, @Valid @RequestBody ReviewIdeiaRequest request) {
		return ideaService.review(id, request, SecurityUtils.currentUser());
	}
}
