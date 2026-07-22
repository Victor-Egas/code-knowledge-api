package com.fadetech.api.knowledge.service.impl;


import com.fadetech.api.knowledge.handler.ChatServiceException;
import com.fadetech.api.knowledge.model.ChatResponse;
import com.fadetech.api.knowledge.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    private final ChatClient chatClient;

    public ChatServiceImpl(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Mono<ChatResponse> generarRespuesta(String prompt) {
        log.info("Generando respuesta completa para prompt: {}", prompt);

        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content()
                .collectList()
                .map(tokens -> String.join("", tokens))
                .map(respuesta -> ChatResponse.of(prompt, respuesta))
                .doOnError(ex -> log.error("Error consultando al modelo", ex))
                .onErrorMap(ex -> new ChatServiceException(
                        "No se pudo generar una respuesta. Verifica que Ollama este corriendo.", ex));
    }

    @Override
    public Flux<String> generarRespuestaStream(String prompt) {
        log.debug("Generando respuesta en streaming para prompt: {}", prompt);

        return chatClient.prompt()
                .user(prompt)
                .stream()
                .content()
                .doOnError(ex -> log.error("Error en streaming del modelo", ex))
                .onErrorMap(ex -> new ChatServiceException(
                        "No se pudo generar una respuesta en streaming. Verifica que Ollama este corriendo.", ex));
    }

}
