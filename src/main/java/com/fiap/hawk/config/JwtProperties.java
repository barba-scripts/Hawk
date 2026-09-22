package com.fiap.hawk.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hawk.jwt")
public class JwtProperties {

	private String issuer = "hawk-api";
	private long accessTokenExpirationSeconds = 3600;
	private long refreshTokenExpirationSeconds = 604800;

	public String getIssuer() {
		return issuer;
	}

	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}

	public long getAccessTokenExpirationSeconds() {
		return accessTokenExpirationSeconds;
	}

	public void setAccessTokenExpirationSeconds(long accessTokenExpirationSeconds) {
		this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
	}

	public long getRefreshTokenExpirationSeconds() {
		return refreshTokenExpirationSeconds;
	}

	public void setRefreshTokenExpirationSeconds(long refreshTokenExpirationSeconds) {
		this.refreshTokenExpirationSeconds = refreshTokenExpirationSeconds;
	}
}
