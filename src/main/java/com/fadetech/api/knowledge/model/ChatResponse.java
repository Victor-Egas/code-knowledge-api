package com.fadetech.api.knowledge.model;

import java.time.Instant;

public record ChatResponse(String prompt,
                           String respuesta,
                           Instant timestamp) {
    public static ChatResponse of(String prompt, String respuesta) {
        return new ChatResponse(prompt, respuesta, Instant.now());
    }
}
