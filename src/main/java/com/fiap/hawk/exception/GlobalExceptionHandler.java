package com.fiap.hawk.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponse> handleApi(ApiException ex) {
		return ResponseEntity.status(ex.getStatus()).body(error(ex.getCode(), ex.getMessage(), ex.getFieldErrors()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
		Map<String, String> fieldErrors = new LinkedHashMap<>();
		for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
			fieldErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
		}
		return ResponseEntity.badRequest().body(error("VALIDATION_ERROR", "Há campos inválidos.", fieldErrors));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorResponse> handleConstraint(ConstraintViolationException ex) {
		return ResponseEntity.badRequest().body(error("VALIDATION_ERROR", ex.getMessage(), null));
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
		return ResponseEntity.badRequest().body(error("VALIDATION_ERROR", "JSON inválido ou campos com tipo incorreto.", null));
	}

	@ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
	public ResponseEntity<ErrorResponse> handleAuth(RuntimeException ex) {
		return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(error("UNAUTHORIZED", "Credenciais inválidas ou token ausente/expirado.", null));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN)
				.body(error("FORBIDDEN", "Você não tem permissão para esta operação.", null));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
		ex.printStackTrace();
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(error("INTERNAL_ERROR", "Erro inesperado.", null));
	}

	private ErrorResponse error(String code, String message, Map<String, String> fieldErrors) {
		String traceId = MDC.get("traceId");
		if (traceId == null || traceId.isBlank()) {
			traceId = UUID.randomUUID().toString().substring(0, 8);
		}
		return new ErrorResponse(code, message, fieldErrors, traceId);
	}
}
