-- ============================================================================
-- SITFAI ERP — Flyway Migration: V7__init_audit_events_schema.sql
-- Shared Kernel: Audit & Event Store (AUD-03, MT-01)
-- Protocolo: MCP-01 / Clean Architecture / Multitenancy MT-01
-- ============================================================================

CREATE TABLE IF NOT EXISTS audit_domain_events (
    id VARCHAR(36) NOT NULL,
    aggregate_id VARCHAR(36),
    event_type VARCHAR(150) NOT NULL,
    payload LONGTEXT NOT NULL,
    empresa_id VARCHAR(36),
    occurred_on DATETIME(6) NOT NULL,

    CONSTRAINT pk_audit_domain_events PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices para trazabilidad y consultas multitenant
CREATE INDEX idx_audit_domain_events_empresa_occurred ON audit_domain_events (empresa_id, occurred_on);
CREATE INDEX idx_audit_domain_events_aggregate_id ON audit_domain_events (aggregate_id);
CREATE INDEX idx_audit_domain_events_event_type ON audit_domain_events (event_type);
