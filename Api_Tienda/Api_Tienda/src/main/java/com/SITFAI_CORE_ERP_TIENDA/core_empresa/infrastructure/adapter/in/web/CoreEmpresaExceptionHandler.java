package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaInvalidaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.RucInvalidoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

/**
 * Manejador Global de Excepciones para el Bounded Context Core-Empresa (Tenant Raíz).
 * <p>
 * Implementa el estándar RFC 7807 (Problem Details) según la REGLA-5.
 */
import com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception.OptimisticConcurrencyException;

@RestControllerAdvice(basePackages = "com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.in.web")
public class CoreEmpresaExceptionHandler {

    @ExceptionHandler(OptimisticConcurrencyException.class)
    public ResponseEntity<ProblemDetail> handleOptimisticConcurrency(OptimisticConcurrencyException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
        problem.setTitle("Conflicto de Concurrencia (CONC-01)");
        problem.setType(URI.create("urn:problem-type:optimistic-concurrency"));
        problem.setProperty("codigoError", "CONCURRENCY_CONFLICT");
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(EmpresaNoEncontradaException.class)
    public ResponseEntity<ProblemDetail> handleEmpresaNoEncontrada(EmpresaNoEncontradaException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problem.setTitle("Empresa No Encontrada");
        problem.setType(URI.create("urn:problem-type:empresa-no-encontrada"));
        problem.setProperty("codigoError", "EMPRESA_NO_ENCONTRADA");
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(EmpresaInvalidaException.class)
    public ResponseEntity<ProblemDetail> handleEmpresaInvalida(EmpresaInvalidaException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );
        problem.setTitle("Violación de Invariante de Empresa");
        problem.setType(URI.create("urn:problem-type:empresa-invalida"));
        problem.setProperty("codigoError", "EMPRESA_INVALIDA");
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(RucInvalidoException.class)
    public ResponseEntity<ProblemDetail> handleRucInvalido(RucInvalidoException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );
        problem.setTitle("Formato de RUC Inválido");
        problem.setType(URI.create("urn:problem-type:ruc-invalido"));
        problem.setProperty("codigoError", "RUC_INVALIDO");
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
        problem.setProperty("codigoError", "DOMINIO_ERROR");
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
        problem.setProperty("codigoError", "ARGUMENTO_INVALIDO");
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problem);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ha ocurrido un error interno no esperado en el servidor de core-empresa."
        );
        problem.setTitle("Error Interno del Servidor");
        problem.setType(URI.create("urn:problem-type:internal-server-error"));
        problem.setProperty("detalle", ex.getMessage());
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
