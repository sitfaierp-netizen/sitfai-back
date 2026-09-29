-- =============================================================================
-- SITFAI ERP — Migración V41: Esquema de Event Store (Módulo core_audit)
-- Bounded Context: core_audit
-- Stack: MySQL 8.4 LTS / InnoDB / utf8mb4
-- Reglas: AUD-03 (Almacenamiento inmutable de eventos), MT-01 (Aislamiento Multi-Tenant)
-- =============================================================================

CREATE TABLE IF NOT EXISTS core_audit_event_store (
    id BINARY(16) NOT NULL,
    empresa_id BINARY(16) NOT NULL,
    nombre_evento VARCHAR(150) NOT NULL,
    ocurrido_en DATETIME(6) NOT NULL,
    payload LONGTEXT NOT NULL,
    estado VARCHAR(30) NOT NULL,
    motivo_fallo VARCHAR(500),
    procesado_en DATETIME(6),
    creado_en DATETIME(6) NOT NULL,
    creado_por VARCHAR(100) NOT NULL,
    actualizado_en DATETIME(6),
    actualizado_por VARCHAR(100),

    CONSTRAINT pk_core_audit_event_store PRIMARY KEY (id),
    CONSTRAINT uq_core_audit_event_store_empresa_id UNIQUE (empresa_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices de partición multi-tenant (MT-01) y despacho Outbox
CREATE INDEX idx_core_audit_event_store_empresa_estado ON core_audit_event_store (empresa_id, estado);
CREATE INDEX idx_core_audit_event_store_empresa_ocurrido ON core_audit_event_store (empresa_id, ocurrido_en);
CREATE INDEX idx_core_audit_event_store_empresa_evento ON core_audit_event_store (empresa_id, nombre_evento);
