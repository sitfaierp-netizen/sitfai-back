package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.BodegaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.StockInsuficienteException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

/**
 * Manejador Global de Excepciones para el Bounded Context de Inventario.
 * <p>
 * Implementa el estándar RFC 7807 (Problem Details) según la REGLA-5.
 * Traduce excepciones de Dominio a respuestas HTTP semánticamente ricas.
 */
@RestControllerAdvice(basePackages = "com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web")
public class InventoryExceptionHandler {

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ProblemDetail> handleStockInsuficiente(StockInsuficienteException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );
        problem.setTitle("Violación de Invariante de Stock (BOD-05)");
        problem.setType(URI.create("urn:problem-type:stock-insuficiente"));
        problem.setProperty("codigoError", ex.getCodigoError());
        problem.setProperty("bodegaId", ex.getBodegaId().toString());
        problem.setProperty("productoId", ex.getProductoId().toString());
        problem.setProperty("stockDisponible", ex.getStockDisponible());
        problem.setProperty("cantidadSolicitada", ex.getCantidadSolicitada());
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(BodegaNoEncontradaException.class)
    public ResponseEntity<ProblemDetail> handleBodegaNoEncontrada(BodegaNoEncontradaException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problem.setTitle("Bodega No Encontrada");
        problem.setType(URI.create("urn:problem-type:bodega-not-found"));
        problem.setProperty("codigoError", ex.getCodigoError());
        problem.setProperty("bodegaId", ex.getBodegaId().toString());
        problem.setProperty("empresaId", ex.getEmpresaId().toString());
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(DomainException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setTitle("Regla de Dominio Violada");
        problem.setType(URI.create("urn:problem-type:domain-rule-violation"));
        problem.setProperty("codigoError", ex.getCodigoError());
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setTitle("Solicitud Inválida");
        problem.setType(URI.create("urn:problem-type:invalid-argument"));
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problem.setTitle("Conflicto de Estado");
        problem.setType(URI.create("urn:problem-type:state-conflict"));
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }
}
