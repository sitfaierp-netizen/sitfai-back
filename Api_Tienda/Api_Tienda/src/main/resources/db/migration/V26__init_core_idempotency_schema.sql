-- V26__init_core_idempotency_schema.sql
-- Migración Flyway para el módulo de Idempotencia

CREATE TABLE core_idempotency_record (
    id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    idempotency_key VARCHAR(64) NOT NULL,
    request_hash VARCHAR(64) NOT NULL,
    status VARCHAR(20) NOT NULL,
    http_status INT,
    response_body TEXT,
    created_at TIMESTAMP NOT NULL,
    
    CONSTRAINT pk_core_idempotency PRIMARY KEY (id),
    CONSTRAINT uq_core_idempotency_empresa_key UNIQUE (empresa_id, idempotency_key)
);
