package com.fadetech.api.knowledge.service;

import com.fadetech.api.knowledge.model.DocumentUploadResponse;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface DocumentIngestionService {

    /**
     * Procesa un archivo subido: lo lee, lo trocea en fragmentos,
     * genera sus embeddings y los guarda en la base de datos vectorial.
     */
    Mono<DocumentUploadResponse> ingestar(FilePart filePart);

}
