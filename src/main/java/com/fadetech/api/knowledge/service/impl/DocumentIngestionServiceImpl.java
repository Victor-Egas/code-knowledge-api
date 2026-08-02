package com.fadetech.api.knowledge.service.impl;

import com.fadetech.api.knowledge.handler.ChatServiceException;
import com.fadetech.api.knowledge.model.DocumentUploadResponse;
import com.fadetech.api.knowledge.service.DocumentIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class DocumentIngestionServiceImpl implements DocumentIngestionService {

    private static final Logger log = LoggerFactory.getLogger(DocumentIngestionServiceImpl.class);

    private final VectorStore vectorStore;

    public DocumentIngestionServiceImpl(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    @Override
    public Mono<DocumentUploadResponse> ingestar(FilePart filePart) {
        String nombreArchivo = filePart.filename();
        log.info("Iniciando ingesta del archivo: {}", nombreArchivo);

        return crearArchivoTemporal(nombreArchivo)
                .flatMap(tempPath -> filePart.transferTo(tempPath)
                        // transferTo es reactivo (escribe a disco de forma no bloqueante)
                        .then(procesarYLimpiar(tempPath, nombreArchivo)))
                .doOnError(ex -> log.error("Error ingiriendo el archivo {}", nombreArchivo, ex))
                .onErrorMap(ex -> !(ex instanceof ChatServiceException), ex -> new ChatServiceException(
                        "No se pudo procesar el documento '" + nombreArchivo + "'.", ex));
    }

    private Mono<Path> crearArchivoTemporal(String nombreArchivo) {
        return Mono.fromCallable(() -> Files.createTempFile("upload-", "-" + nombreArchivo))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<DocumentUploadResponse> procesarYLimpiar(Path tempPath, String nombreArchivo) {
        return Mono.fromCallable(() -> procesarArchivo(tempPath, nombreArchivo))
                // Tika (parseo) y el VectorStore (llamadas de embeddings) son
                // bloqueantes, asi que corremos todo en un scheduler aparte
                // para no bloquear el event loop de WebFlux.
                .subscribeOn(Schedulers.boundedElastic())
                .doFinally(signal -> borrarArchivoTemporal(tempPath));
    }

    private DocumentUploadResponse procesarArchivo(Path tempPath, String nombreArchivo) {
        // 1. Leer el documento (Tika soporta PDF, DOCX, PPTX, TXT, HTML, etc.)
        TikaDocumentReader reader = new TikaDocumentReader(new FileSystemResource(tempPath.toFile()));
        List<Document> documentos = reader.read();

        // 2. Trocear en fragmentos chicos (un documento entero no entra
        //    en el contexto del modelo, y fragmentos chicos dan busquedas
        //    semanticas mas precisas mas adelante en la Fase 4)
        TokenTextSplitter splitter = TokenTextSplitter.builder().build();
        List<Document> fragmentos = splitter.apply(documentos);

        // 3. Agregar metadata util para poder filtrar/trazar el origen
        //    de cada fragmento cuando hagamos RAG
        fragmentos.forEach(fragmento -> fragmento.getMetadata().put("nombreArchivo", nombreArchivo));

        // 4. Generar embeddings (via Ollama) y guardar en la base vectorial.
        //    Este add() es el que internamente llama al EmbeddingModel.
        vectorStore.add(fragmentos);

        log.info("Archivo '{}' indexado: {} fragmentos generados", nombreArchivo, fragmentos.size());
        return DocumentUploadResponse.of(nombreArchivo, fragmentos.size());
    }

    private void borrarArchivoTemporal(Path tempPath) {
        try {
            Files.deleteIfExists(tempPath);
        } catch (IOException ex) {
            log.warn("No se pudo borrar el archivo temporal {}", tempPath, ex);
        }
    }

}
