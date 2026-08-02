package com.fadetech.api.knowledge.web;

import com.fadetech.api.knowledge.model.ChatResponse;
import com.fadetech.api.knowledge.service.ChatService;
import com.fadetech.api.knowledge.service.RagChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


@RequestMapping("/api/chat")
@RestController
public class ChatController {

    private final ChatService chatService;
    private final RagChatService ragChatService;

    public ChatController(ChatService chatService, RagChatService ragChatService) {
        this.chatService = chatService;
        this.ragChatService = ragChatService;
    }

    /**
     * Respuesta completa, con memoria de conversacion, SIN buscar en documentos.
     * Ejemplo: GET /api/chat?prompt=Hola&conversationId=usuario-123
     *
     * Si no mandas conversationId, todos los requests van a "default"
     * (util para pruebas rapidas, pero en un caso real cada usuario/sesion
     * deberia tener su propio id).
     */
    @GetMapping
    public Mono<ChatResponse> chat(
            @RequestParam(defaultValue = "Hola, quien eres?") String prompt,
            @RequestParam(defaultValue = "default") String conversationId) {
        return chatService.generarRespuesta(prompt, conversationId);
    }

    /**
     * Respuesta completa, buscando contexto en los documentos indexados (RAG).
     * Ejemplo: GET /api/chat/rag?prompt=Que dice el documento sobre X?&conversationId=usuario-123
     */
    @GetMapping("/rag")
    public Mono<ChatResponse> chatRag(
            @RequestParam(defaultValue = "Hola, quien eres?") String prompt,
            @RequestParam(defaultValue = "default") String conversationId) {
        return ragChatService.generarRespuesta(prompt, conversationId);
    }

    /**
     * Respuesta en streaming, buscando contexto en los documentos indexados (RAG).
     * Ejemplo: GET /api/chat/rag/stream?prompt=Que dice el documento sobre X?&conversationId=usuario-123
     */
    @GetMapping(value = "/rag/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatRagStream(
            @RequestParam(defaultValue = "Hola, quien eres?") String prompt,
            @RequestParam(defaultValue = "default") String conversationId) {
        return ragChatService.generarRespuestaStream(prompt, conversationId);
    }

    /**
     * Borra el historial de una conversacion puntual (aplica a ambos chats,
     * porque comparten el mismo ChatMemory).
     * Ejemplo: DELETE /api/chat/usuario-123
     */
    @DeleteMapping("/{conversationId}")
    public Mono<Void> limpiarConversacion(@PathVariable String conversationId) {
        return Mono.fromRunnable(() -> chatService.limpiarConversacion(conversationId));
    }

}
