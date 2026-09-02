package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.exception;

public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException(String message) {
        super(message);
    }
}
