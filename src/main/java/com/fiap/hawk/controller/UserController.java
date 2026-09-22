package com.fiap.hawk.controller;

import com.fiap.hawk.dto.response.UserResponse;
import com.fiap.hawk.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public List<UserResponse> list(
			@RequestParam(required = false) String role,
			@RequestParam(required = false) String division,
			@RequestParam(required = false) Boolean active) {
		return userService.list(role, division, active);
	}
}
