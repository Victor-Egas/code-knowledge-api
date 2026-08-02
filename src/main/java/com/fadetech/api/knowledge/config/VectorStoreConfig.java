package com.fadetech.api.knowledge.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configura la base de datos vectorial. Por ahora usamos SimpleVectorStore
 * (en memoria, sin instalar nada externo) para arrancar simple.
 *
 * Mas adelante, si se necesita persistencia real, esto se cambia por
 * PgVectorStore (Postgres) u otro proveedor, sin tocar el resto del
 * codigo, porque todo el sistema habla contra la interfaz VectorStore.
 */
@Configuration
public class VectorStoreConfig {

    @Bean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

}
