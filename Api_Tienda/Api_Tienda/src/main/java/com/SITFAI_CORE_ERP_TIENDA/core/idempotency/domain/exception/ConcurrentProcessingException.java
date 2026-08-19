package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.exception;

public class ConcurrentProcessingException extends RuntimeException {
    public ConcurrentProcessingException(String message) {
        super(message);
    }
}
