package com.fadetech.api.knowledge.web;

import com.fadetech.api.knowledge.model.DocumentUploadResponse;
import com.fadetech.api.knowledge.service.DocumentIngestionService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentIngestionService documentIngestionService;

    public DocumentController(DocumentIngestionService documentIngestionService) {
        this.documentIngestionService = documentIngestionService;
    }

    /**
     * Sube un documento (PDF, DOCX, TXT, etc.), lo trocea, genera sus
     * embeddings y lo guarda en la base de datos vectorial.
     *
     * En Postman: Body -> form-data -> key "file" tipo File.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<DocumentUploadResponse> subirDocumento(@RequestPart("file") Mono<FilePart> filePart) {
        return filePart.flatMap(documentIngestionService::ingestar);
    }

}
