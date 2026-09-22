package com.fiap.hawk.service;

import com.fiap.hawk.domain.Role;
import com.fiap.hawk.domain.UserDocument;
import com.fiap.hawk.dto.response.UserResponse;
import com.fiap.hawk.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

	private final UserRepository userRepository;

	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<UserResponse> list(String role, String division, Boolean active) {
		return userRepository.findAll().stream()
				.filter(user -> role == null || role.isBlank() || user.getRole() == Role.fromValue(role))
				.filter(user -> division == null || division.isBlank() || division.equalsIgnoreCase(user.getDivision()))
				.filter(user -> active == null || user.isActive() == active)
				.map(UserResponse::from)
				.toList();
	}

	public UserDocument requireByPublicId(Long publicId) {
		return userRepository.findByPublicId(publicId)
				.orElseThrow(() -> com.fiap.hawk.exception.ApiException.notFound("Usuário não encontrado."));
	}
}
