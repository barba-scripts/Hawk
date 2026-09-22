package com.fiap.hawk.controller;

import com.fiap.hawk.dto.request.LoginRequest;
import com.fiap.hawk.dto.request.RefreshRequest;
import com.fiap.hawk.dto.response.LoginResponse;
import com.fiap.hawk.dto.response.UserResponse;
import com.fiap.hawk.security.SecurityUtils;
import com.fiap.hawk.security.UserPrincipal;
import com.fiap.hawk.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/login")
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/refresh")
	public LoginResponse refresh(@Valid @RequestBody RefreshRequest request) {
		return authService.refresh(request.refreshToken());
	}

	@GetMapping("/me")
	public UserResponse me() {
		return authService.me(SecurityUtils.currentUser());
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshRequest request) {
		UserPrincipal principal = SecurityUtils.currentUser();
		String refreshToken = request != null ? request.refreshToken() : null;
		authService.logout(principal, refreshToken);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
	}
}
