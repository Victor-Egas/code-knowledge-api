package com.fadetech.api.knowledge.service;

import com.fadetech.api.knowledge.model.ChatResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ChatService {

    /**
     * Genera una respuesta completa (no streaming) para el prompt dado.
     */
    Mono<ChatResponse> generarRespuesta(String prompt);

    /**
     * Genera la respuesta en forma de stream, token por token.
     */
    Flux<String> generarRespuestaStream(String prompt);

}
