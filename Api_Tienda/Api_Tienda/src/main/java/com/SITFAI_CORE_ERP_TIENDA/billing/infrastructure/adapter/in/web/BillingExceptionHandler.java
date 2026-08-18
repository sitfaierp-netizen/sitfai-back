package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.time.Instant;

/**
 * Interceptor de Excepciones del Módulo Billing.
 * Cumple con RFC 7807 Problem Details for HTTP APIs.
 */
@RestControllerAdvice(basePackages = "com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web")
public class BillingExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(BillingExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex) {
        log.warn("Violación de Invariante de Dominio (Billing): {}", ex.getMessage());
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problem.setType(URI.create("https://api.sitfai.com/errors/billing/domain-invariant-violation"));
        problem.setTitle("Error de Negocio DIAN");
        problem.setProperty("timestamp", Instant.now());
        
        return problem;
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Argumento inválido en Billing: {}", ex.getMessage());
        
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setType(URI.create("https://api.sitfai.com/errors/billing/invalid-argument"));
        problem.setTitle("Petición Inválida");
        problem.setProperty("timestamp", Instant.now());
        
        return problem;
    }
}
