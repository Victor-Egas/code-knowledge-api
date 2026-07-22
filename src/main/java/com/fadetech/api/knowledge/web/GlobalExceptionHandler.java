package com.fadetech.api.knowledge.web;

import com.fadetech.api.knowledge.handler.ChatServiceException;
import com.fadetech.api.knowledge.model.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ChatServiceException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleChatServiceException(ChatServiceException ex) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Service Unavailable",
                ex.getMessage()
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(WebExchangeBindException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Datos de entrada invalidos");

        ErrorResponse body = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                mensaje
        );
        return Mono.just(ResponseEntity.badRequest().body(body));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex) {
        ErrorResponse body = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ocurrio un error inesperado."
        );
        return Mono.just(ResponseEntity.internalServerError().body(body));
    }
}
