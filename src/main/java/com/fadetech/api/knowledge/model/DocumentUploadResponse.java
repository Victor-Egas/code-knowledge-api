package com.fadetech.api.knowledge.model;

import java.time.Instant;

/**
 * Confirmacion de que un documento fue procesado e indexado en la
 * base de datos vectorial.
 */
public record DocumentUploadResponse(
        String nombreArchivo,
        int fragmentosGenerados,
        Instant timestamp
) {
    public static DocumentUploadResponse of(String nombreArchivo, int fragmentosGenerados) {
        return new DocumentUploadResponse(nombreArchivo, fragmentosGenerados, Instant.now());
    }
}
