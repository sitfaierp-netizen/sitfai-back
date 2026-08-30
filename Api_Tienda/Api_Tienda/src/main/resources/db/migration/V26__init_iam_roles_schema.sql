-- ============================================================================
-- SITFAI ERP — Flyway Migration: V26__init_iam_roles_schema.sql
-- Bounded Context: iam-module (Identity & Access Management - Puerto 8082)
-- Description: Estructura de Roles y Matriz de Permisos
-- ============================================================================

CREATE TABLE IF NOT EXISTS iam_rol (
    id VARCHAR(36) NOT NULL,
    codigo VARCHAR(50) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    CONSTRAINT pk_iam_rol PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS iam_rol_permiso (
    rol_id VARCHAR(36) NOT NULL,
    modulo VARCHAR(50) NOT NULL,
    accion VARCHAR(50) NOT NULL,
    CONSTRAINT fk_iam_rol_permiso_rol FOREIGN KEY (rol_id) REFERENCES iam_rol(id) ON DELETE CASCADE
);

CREATE INDEX idx_iam_rol_permiso_rol_id ON iam_rol_permiso (rol_id);
