package com.fiap.hawk.exception;

import org.springframework.http.HttpStatus;

import java.util.Map;

public class ApiException extends RuntimeException {

	private final String code;
	private final HttpStatus status;
	private final Map<String, String> fieldErrors;

	public ApiException(String code, String message, HttpStatus status) {
		this(code, message, status, null);
	}

	public ApiException(String code, String message, HttpStatus status, Map<String, String> fieldErrors) {
		super(message);
		this.code = code;
		this.status = status;
		this.fieldErrors = fieldErrors;
	}

	public String getCode() {
		return code;
	}

	public HttpStatus getStatus() {
		return status;
	}

	public Map<String, String> getFieldErrors() {
		return fieldErrors;
	}

	public static ApiException badRequest(String message) {
		return new ApiException("VALIDATION_ERROR", message, HttpStatus.BAD_REQUEST);
	}

	public static ApiException unauthorized(String message) {
		return new ApiException("UNAUTHORIZED", message, HttpStatus.UNAUTHORIZED);
	}

	public static ApiException forbidden(String message) {
		return new ApiException("FORBIDDEN", message, HttpStatus.FORBIDDEN);
	}

	public static ApiException notFound(String message) {
		return new ApiException("NOT_FOUND", message, HttpStatus.NOT_FOUND);
	}

	public static ApiException conflict(String message) {
		return new ApiException("CONFLICT", message, HttpStatus.CONFLICT);
	}
}
