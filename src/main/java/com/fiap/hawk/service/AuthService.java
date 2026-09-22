package com.fiap.hawk.service;

import com.fiap.hawk.config.JwtProperties;
import com.fiap.hawk.domain.RefreshTokenDocument;
import com.fiap.hawk.domain.UserDocument;
import com.fiap.hawk.dto.request.LoginRequest;
import com.fiap.hawk.dto.response.LoginResponse;
import com.fiap.hawk.dto.response.UserResponse;
import com.fiap.hawk.exception.ApiException;
import com.fiap.hawk.repository.RefreshTokenRepository;
import com.fiap.hawk.repository.UserRepository;
import com.fiap.hawk.security.JwtService;
import com.fiap.hawk.security.UserPrincipal;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final UserRepository userRepository;
	private final RefreshTokenRepository refreshTokenRepository;
	private final JwtService jwtService;
	private final JwtProperties jwtProperties;

	public AuthService(AuthenticationManager authenticationManager, UserRepository userRepository,
			RefreshTokenRepository refreshTokenRepository, JwtService jwtService, JwtProperties jwtProperties) {
		this.authenticationManager = authenticationManager;
		this.userRepository = userRepository;
		this.refreshTokenRepository = refreshTokenRepository;
		this.jwtService = jwtService;
		this.jwtProperties = jwtProperties;
	}

	public LoginResponse login(LoginRequest request) {
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));

		UserDocument user = userRepository.findByEmailIgnoreCase(request.email())
				.orElseThrow(() -> ApiException.unauthorized("Credenciais inválidas."));

		if (!user.isActive()) {
			throw ApiException.forbidden("Usuário inativo.");
		}

		return issueTokens(user);
	}

	public LoginResponse refresh(String refreshToken) {
		String hash = hash(refreshToken);
		RefreshTokenDocument stored = refreshTokenRepository.findByTokenHash(hash)
				.orElseThrow(() -> ApiException.unauthorized("Refresh token inválido."));

		if (!stored.isActive()) {
			throw ApiException.unauthorized("Refresh token expirado ou revogado.");
		}

		UserDocument user = userRepository.findByPublicId(stored.getUserPublicId())
				.orElseThrow(() -> ApiException.unauthorized("Usuário não encontrado."));

		stored.setRevokedAt(Instant.now());
		refreshTokenRepository.save(stored);

		return issueTokens(user);
	}

	public void logout(UserPrincipal principal, String refreshToken) {
		if (refreshToken != null && !refreshToken.isBlank()) {
			refreshTokenRepository.findByTokenHash(hash(refreshToken)).ifPresent(token -> {
				token.setRevokedAt(Instant.now());
				refreshTokenRepository.save(token);
			});
		} else {
			refreshTokenRepository.deleteByUserPublicId(principal.getPublicId());
		}
	}

	public UserResponse me(UserPrincipal principal) {
		UserDocument user = userRepository.findByPublicId(principal.getPublicId())
				.orElseThrow(() -> ApiException.notFound("Usuário não encontrado."));
		return UserResponse.from(user);
	}

	private LoginResponse issueTokens(UserDocument user) {
		String accessToken = jwtService.generateAccessToken(user);
		String refreshToken = jwtService.generateRefreshTokenValue();

		RefreshTokenDocument refreshDoc = new RefreshTokenDocument();
		refreshDoc.setTokenHash(hash(refreshToken));
		refreshDoc.setUserPublicId(user.getPublicId());
		refreshDoc.setCreatedAt(Instant.now());
		refreshDoc.setExpiresAt(Instant.now().plusSeconds(jwtProperties.getRefreshTokenExpirationSeconds()));
		refreshTokenRepository.save(refreshDoc);

		return new LoginResponse(
				accessToken,
				jwtService.getAccessTokenExpirationSeconds(),
				refreshToken,
				UserResponse.fromLogin(user)
		);
	}

	private String hash(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(hashed);
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
}
