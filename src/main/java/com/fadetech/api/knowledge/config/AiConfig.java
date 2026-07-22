package com.fadetech.api.knowledge.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Centraliza la construccion del ChatClient.
 * Separarlo en una clase de configuracion (en vez de armarlo dentro del
 * controller) facilita agregarle mas adelante un ChatMemory, un
 * defaultSystem prompt, o advisors de RAG, sin tocar el controller ni el
 * service.
 */
@Configuration
public class AiConfig {

    @Bean
    public ChatClient chatClient(ChatClient.Builder chatClientBuilder) {
        return chatClientBuilder
                .defaultSystem("Sos un asistente util que responde en español de forma clara y concisa.")
                .build();
    }

}
