package com.fiap.hawk.security;

import com.fiap.hawk.config.JwtProperties;
import com.fiap.hawk.domain.JwtKeyDocument;
import com.fiap.hawk.domain.Role;
import com.fiap.hawk.domain.UserDocument;
import com.fiap.hawk.repository.JwtKeyRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.HexFormat;
import java.util.Map;

@Service
public class JwtService {

	private final JwtKeyRepository jwtKeyRepository;
	private final JwtProperties jwtProperties;
	private volatile SecretKey secretKey;

	public JwtService(JwtKeyRepository jwtKeyRepository, JwtProperties jwtProperties) {
		this.jwtKeyRepository = jwtKeyRepository;
		this.jwtProperties = jwtProperties;
	}

	@PostConstruct
	public void init() {
		reloadKey();
	}

	public synchronized void reloadKey() {
		JwtKeyDocument key = jwtKeyRepository.findById(JwtKeyDocument.ACTIVE_KEY_ID)
				.or(() -> jwtKeyRepository.findByActiveTrue())
				.orElseGet(this::createAndPersistKey);
		this.secretKey = Keys.hmacShaKeyFor(key.getSecret().getBytes(StandardCharsets.UTF_8));
	}

	private JwtKeyDocument createAndPersistKey() {
		byte[] bytes = new byte[64];
		new SecureRandom().nextBytes(bytes);
		String secret = Base64.getEncoder().encodeToString(bytes);
		JwtKeyDocument doc = new JwtKeyDocument();
		doc.setId(JwtKeyDocument.ACTIVE_KEY_ID);
		doc.setSecret(secret);
		doc.setActive(true);
		doc.setCreatedAt(Instant.now());
		doc.setRotatedAt(Instant.now());
		return jwtKeyRepository.save(doc);
	}

	public String generateAccessToken(UserDocument user) {
		Instant now = Instant.now();
		Instant exp = now.plusSeconds(jwtProperties.getAccessTokenExpirationSeconds());
		return Jwts.builder()
				.issuer(jwtProperties.getIssuer())
				.subject(String.valueOf(user.getPublicId()))
				.claims(Map.of(
						"role", user.getRole().getValue(),
						"division", user.getDivision() == null ? "" : user.getDivision(),
						"email", user.getEmail()
				))
				.issuedAt(Date.from(now))
				.expiration(Date.from(exp))
				.signWith(secretKey)
				.compact();
	}

	public String generateRefreshTokenValue() {
		byte[] bytes = new byte[32];
		new SecureRandom().nextBytes(bytes);
		return HexFormat.of().formatHex(bytes);
	}

	public Claims parseClaims(String token) {
		return Jwts.parser()
				.verifyWith(secretKey)
				.requireIssuer(jwtProperties.getIssuer())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	public Integer extractUserId(String token) {
		return Integer.valueOf(parseClaims(token).getSubject());
	}

	public Role extractRole(String token) {
		return Role.fromValue(parseClaims(token).get("role", String.class));
	}

	public long getAccessTokenExpirationSeconds() {
		return jwtProperties.getAccessTokenExpirationSeconds();
	}

	public long getRefreshTokenExpirationSeconds() {
		return jwtProperties.getRefreshTokenExpirationSeconds();
	}
}
