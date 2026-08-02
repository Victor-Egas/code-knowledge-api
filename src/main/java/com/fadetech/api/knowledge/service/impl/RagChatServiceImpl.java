package com.fadetech.api.knowledge.service.impl;

import com.fadetech.api.knowledge.handler.ChatServiceException;
import com.fadetech.api.knowledge.model.ChatResponse;
import com.fadetech.api.knowledge.service.RagChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class RagChatServiceImpl implements RagChatService {

    private static final Logger log = LoggerFactory.getLogger(RagChatServiceImpl.class);

    private final ChatClient ragChatClient;

    public RagChatServiceImpl(@Qualifier("ragChatClient") ChatClient ragChatClient) {
        this.ragChatClient = ragChatClient;
    }

    @Override
    public Mono<ChatResponse> generarRespuesta(String prompt, String conversationId) {
        log.debug("Generando respuesta RAG para conversationId={}, prompt={}", conversationId, prompt);

        return ragChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()
                .collectList()
                .map(tokens -> String.join("", tokens))
                .map(respuesta -> ChatResponse.of(prompt, respuesta))
                .doOnError(ex -> log.error("Error consultando al modelo con RAG", ex))
                .onErrorMap(ex -> new ChatServiceException(
                        "No se pudo generar una respuesta con RAG. Verifica que Ollama este corriendo "
                                + "y que haya documentos indexados.", ex));
    }

    @Override
    public Flux<String> generarRespuestaStream(String prompt, String conversationId) {
        log.debug("Generando respuesta RAG en streaming para conversationId={}, prompt={}", conversationId, prompt);

        return ragChatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                .stream()
                .content()
                .doOnError(ex -> log.error("Error en streaming RAG del modelo", ex))
                .onErrorMap(ex -> new ChatServiceException(
                        "No se pudo generar una respuesta en streaming con RAG. Verifica que Ollama "
                                + "este corriendo y que haya documentos indexados.", ex));
    }

}
