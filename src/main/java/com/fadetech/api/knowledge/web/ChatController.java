package com.fadetech.api.knowledge.web;

import com.fadetech.api.knowledge.model.ChatResponse;
import com.fadetech.api.knowledge.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    /**
     * Respuesta completa, no streaming.
     * Ejemplo: GET /api/chat?prompt=Hola
     */
    @GetMapping("/api/chat")
    public Mono<ChatResponse> chat(@RequestParam(defaultValue = "Hola, quien eres?") String prompt) {
        return chatService.generarRespuesta(prompt);
    }

    /**
     * Respuesta en streaming, token por token, via Server-Sent Events.
     * Ejemplo: GET /api/chat/stream?prompt=Hola
     */
    @GetMapping(value = "/api/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam(defaultValue = "Hola, quien eres?") String prompt) {
        return chatService.generarRespuestaStream(prompt);
    }

}
