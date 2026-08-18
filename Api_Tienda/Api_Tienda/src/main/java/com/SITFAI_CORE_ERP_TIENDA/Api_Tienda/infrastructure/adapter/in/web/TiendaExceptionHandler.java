package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

/**
 * Manejador Global de Excepciones para el Bounded Context de Ventas / POS (Api_Tienda).
 * <p>
 * Implementa el estándar RFC 7807 (Problem Details) según la REGLA-5.
 * Traduce excepciones de Dominio y de Aplicación a respuestas HTTP semánticas.
 */
@RestControllerAdvice(basePackages = "com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web")
public class TiendaExceptionHandler {

    @ExceptionHandler(PedidoNoEncontradoException.class)
    public ResponseEntity<ProblemDetail> handlePedidoNoEncontrado(PedidoNoEncontradoException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problem.setTitle("Pedido No Encontrado");
        problem.setType(URI.create("urn:problem-type:pedido-no-encontrado"));
        problem.setProperty("codigoError", ex.getCodigoError());
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(PedidoInvalidoException.class)
    public ResponseEntity<ProblemDetail> handlePedidoInvalido(PedidoInvalidoException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );
        problem.setTitle("Violación de Invariante de Pedido");
        problem.setType(URI.create("urn:problem-type:pedido-invalido"));
        problem.setProperty("codigoError", ex.getCodigoError());
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
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
        problem.setTitle("Argumento Inválido");
        problem.setType(URI.create("urn:problem-type:illegal-argument"));
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ha ocurrido un error interno no esperado en el servidor."
        );
        problem.setTitle("Error Interno del Servidor");
        problem.setType(URI.create("urn:problem-type:internal-server-error"));
        problem.setProperty("detalle", ex.getMessage());
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
