package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.exception.TurnoNoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;

@RestControllerAdvice(basePackageClasses = TurnoCajaController.class)
public class PosExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalStateException(IllegalStateException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
        problemDetail.setTitle("Violación de Regla de Negocio en POS");
        problemDetail.setType(URI.create("https://api.sitfai.com/errors/pos-business-rule"));
        return problemDetail;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgumentException(IllegalArgumentException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problemDetail.setTitle("Argumento Inválido en POS");
        problemDetail.setType(URI.create("https://api.sitfai.com/errors/invalid-argument"));
        return problemDetail;
    }

    @ExceptionHandler(TurnoNoEncontradoException.class)
    public ProblemDetail handleTurnoNoEncontradoException(TurnoNoEncontradoException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Turno No Encontrado");
        problemDetail.setType(URI.create("https://api.sitfai.com/errors/turno-not-found"));
        return problemDetail;
    }
}
