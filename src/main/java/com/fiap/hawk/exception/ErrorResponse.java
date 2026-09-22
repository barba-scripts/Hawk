package com.fiap.hawk.exception;

import java.util.Map;

public record ErrorResponse(
		String code,
		String message,
		Map<String, String> fieldErrors,
		String traceId
) {
}
