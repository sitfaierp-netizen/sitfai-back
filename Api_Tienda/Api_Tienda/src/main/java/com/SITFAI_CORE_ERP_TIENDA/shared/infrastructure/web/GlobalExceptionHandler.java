package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web;

import com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception.DocumentStateException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(DocumentStateException.class)
    public ProblemDetail handleDocumentStateException(DocumentStateException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Estado Invalido");
        problemDetail.setType(URI.create("https://api.sitfai.com/errors/conflict"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }

    @ExceptionHandler(com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception.RegistroDuplicadoException.class)
    public ProblemDetail handleRegistroDuplicadoException(com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception.RegistroDuplicadoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problemDetail.setTitle("Registro Duplicado");
        problemDetail.setType(URI.create("https://api.sitfai.com/errors/conflict"));
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("entidad", ex.getEntidad());
        problemDetail.setProperty("campo", ex.getCampo());
        problemDetail.setProperty("valor", ex.getValor());
        return problemDetail;
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "El registro viola las reglas de unicidad o integridad de datos en la base de datos.");
        problemDetail.setTitle("Violación de Integridad de Datos");
        problemDetail.setType(URI.create("https://api.sitfai.com/errors/conflict"));
        problemDetail.setProperty("timestamp", Instant.now());
        return problemDetail;
    }
}
