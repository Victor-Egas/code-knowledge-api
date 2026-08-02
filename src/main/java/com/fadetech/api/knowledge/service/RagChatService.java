package com.fadetech.api.knowledge.service;

import com.fadetech.api.knowledge.model.ChatResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Igual que ChatService, pero las respuestas se generan buscando contexto
 * relevante en los documentos indexados en el VectorStore (RAG).
 */
public interface RagChatService {

    Mono<ChatResponse> generarRespuesta(String prompt, String conversationId);

    Flux<String> generarRespuestaStream(String prompt, String conversationId);

}
