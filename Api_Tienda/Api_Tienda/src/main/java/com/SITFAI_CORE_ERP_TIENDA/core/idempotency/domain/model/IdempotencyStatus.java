package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model;

public enum IdempotencyStatus {
    PROCESSING,
    COMPLETED,
    FAILED,
    EXPIRED
}
