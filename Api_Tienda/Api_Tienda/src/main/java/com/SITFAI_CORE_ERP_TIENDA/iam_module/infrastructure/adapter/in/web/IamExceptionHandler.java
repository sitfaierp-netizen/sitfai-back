package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.exception.IdentityConflictException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.application.exception.IdentityProvisioningException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;

/**
 * Manejador Global de Excepciones para el Bounded Context IAM Module.
 * Implementa el estándar RFC 7807 (Problem Details) según la REGLA-5.
 */
@RestControllerAdvice(basePackages = "com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.in.web")
public class IamExceptionHandler {

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "No tiene permisos para ejecutar esta operación."
        );
        problem.setTitle("Acceso Denegado");
        problem.setType(URI.create("urn:problem-type:access-denied"));
        problem.setProperty("codigoError", "ACCESS_DENIED");
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(problem);
    }

    @ExceptionHandler(IdentityConflictException.class)
    public ResponseEntity<ProblemDetail> handleIdentityConflict(IdentityConflictException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "La identidad externa existente no corresponde al usuario solicitado."
        );
        problem.setTitle("Conflicto de Identidad Externa");
        problem.setType(URI.create("urn:problem-type:identity-conflict"));
        problem.setProperty("codigoError", "IDENTITY_CONFLICT");
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(problem);
    }

    @ExceptionHandler(IdentityProvisioningException.class)
    public ResponseEntity<ProblemDetail> handleIdentityUnavailable(IdentityProvisioningException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "El proveedor de identidad no está disponible. La operación puede reintentarse de forma segura."
        );
        problem.setTitle("Proveedor de Identidad No Disponible");
        problem.setType(URI.create("urn:problem-type:identity-provider-unavailable"));
        problem.setProperty("codigoError", "IDENTITY_PROVIDER_UNAVAILABLE");
        problem.setProperty("retryable", true);
        problem.setProperty("timestamp", Instant.now());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(problem);
    }

    @ExceptionHandler(UsuarioNoEncontradoException.class)
    public ResponseEntity<ProblemDetail> handleUsuarioNoEncontrado(UsuarioNoEncontradoException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                ex.getMessage()
        );
        problem.setTitle("Usuario No Encontrado");
        problem.setType(URI.create("urn:problem-type:usuario-no-encontrado"));
        problem.setProperty("codigoError", "USUARIO_NO_ENCONTRADO");
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(UsuarioInvalidoException.class)
    public ResponseEntity<ProblemDetail> handleUsuarioInvalido(UsuarioInvalidoException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ex.getMessage()
        );
        problem.setTitle("Violación de Invariante de Usuario");
        problem.setType(URI.create("urn:problem-type:usuario-invalido"));
        problem.setProperty("codigoError", "USUARIO_INVALIDO");
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(problem);
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(DomainException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        problem.setTitle("Regla de Dominio IAM Violada");
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
                "Ha ocurrido un error interno no esperado en el servidor IAM."
        );
        problem.setTitle("Error Interno del Servidor");
        problem.setType(URI.create("urn:problem-type:internal-server-error"));
        problem.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problem);
    }
}
