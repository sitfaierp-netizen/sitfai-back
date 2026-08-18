-- ============================================================================
-- SITFAI ERP — Flyway Migration: V6__init_iam_schema.sql
-- Bounded Context: iam-module (Identity & Access Management - Puerto 8082)
-- Protocolo: MCP-01 / Clean Architecture / Multitenancy MT-01 / Protocolo 4 (Cero Contraseñas)
-- ============================================================================

CREATE TABLE IF NOT EXISTS iam_usuario (
    id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(150) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    creado_en DATETIME(6) NOT NULL,
    actualizado_en DATETIME(6) NOT NULL,

    CONSTRAINT pk_iam_usuario PRIMARY KEY (id),
    CONSTRAINT uq_iam_usuario_empresa_username UNIQUE (empresa_id, username),
    CONSTRAINT uq_iam_usuario_empresa_email UNIQUE (empresa_id, email)
);

-- Índices para optimización de consultas multitenant
CREATE INDEX idx_iam_usuario_empresa_id ON iam_usuario (empresa_id);
CREATE INDEX idx_iam_usuario_empresa_username ON iam_usuario (empresa_id, username);
CREATE INDEX idx_iam_usuario_empresa_email ON iam_usuario (empresa_id, email);
