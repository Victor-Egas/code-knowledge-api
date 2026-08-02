package com.fadetech.api.knowledge.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;

/**
 * Centraliza la construccion de los ChatClient y de la memoria de conversacion.
 */
@Configuration
public class AiConfig {

    /**
     * Memoria de conversacion en memoria RAM (se pierde si reiniciamos la app).
     * MessageWindowChatMemory conserva solo los ultimos N mensajes por
     * conversationId para no mandar un historial infinito al modelo.
     */
    @Bean
    public ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(20)
                .build();
    }

    /**
     * ChatClient "normal": conversa con memoria, pero NO busca en documentos.
     * Es el mismo que ya teniamos desde la Fase 2.
     */
    @Bean("chatClient")
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
        return chatClientBuilder
                .defaultSystem("Sos un asistente util que responde en español de forma clara y concisa.")
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    /**
     * ChatClient de RAG: ademas de la memoria de conversacion, agrega el
     * QuestionAnswerAdvisor, que antes de cada llamada busca en el
     * VectorStore los fragmentos mas parecidos a la pregunta y los inyecta
     * como contexto en el prompt.
     *
     * Usamos un ChatClient.Builder distinto al del bean de arriba: el
     * builder autoconfigurado por Spring AI es "prototype", asi que cada
     * inyeccion nos da una instancia nueva e independiente.
     */
    @Bean("ragChatClient")
    public ChatClient ragChatClient(ChatClient.Builder chatClientBuilder,
                                    ChatMemory chatMemory,
                                    VectorStore vectorStore) {
        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .topK(4)                  // cuantos fragmentos trae como maximo
                        .similarityThreshold(0.5) // que tan "parecido" tiene que ser para incluirlo (0 a 1)
                        .build())
                .build();

        return chatClientBuilder
                .defaultSystem("""
                        Sos un asistente que responde preguntas usando el contexto
                        de documentos que se te provee. Si la respuesta no esta en
                        el contexto, decilo claramente en vez de inventar una
                        respuesta. Respondes en español, de forma clara y concisa.
                        """)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        questionAnswerAdvisor
                )
                .build();
    }

}
