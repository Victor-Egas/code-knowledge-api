package com.fadetech.api.knowledge.model;

import java.time.Instant;

/**
 * Formato uniforme para todas las respuestas de error de la API.
 */
public record ErrorResponse(
        int status,
        String error,
        String message,
        Instant timestamp
) {
    public static ErrorResponse of(int status, String error, String message) {
        return new ErrorResponse(status, error, message, Instant.now());
    }
}
