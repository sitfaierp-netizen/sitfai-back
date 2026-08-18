-- SITFAI ERP — Migración V5: Esquema Inicial de Core Empresa & Multitenant Raíz
-- Stack: MySQL 8.0+ / InnoDB / utf8mb4
-- Reglas aplicadas: REGLA-6 (snake_case, UUIDs VARCHAR(36)), EMP-01 a EMP-07, SUC-01 a SUC-06

-- 1. Tabla: core_empresa (Aggregate Root Empresa / Tenant Raíz)
CREATE TABLE IF NOT EXISTS core_empresa (
    id VARCHAR(36) NOT NULL,
    ruc VARCHAR(20) NOT NULL,
    razon_social VARCHAR(150) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    creado_en DATETIME NOT NULL,
    actualizado_en DATETIME NOT NULL,
    CONSTRAINT pk_core_empresa PRIMARY KEY (id),
    CONSTRAINT uq_core_empresa_ruc UNIQUE (ruc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices de Consulta para Empresa
CREATE INDEX idx_core_empresa_ruc ON core_empresa (ruc);
CREATE INDEX idx_core_empresa_estado ON core_empresa (estado);
CREATE INDEX idx_core_empresa_fecha ON core_empresa (creado_en);

-- 2. Tabla: core_sucursal (Entidad interna de Empresa)
CREATE TABLE IF NOT EXISTS core_sucursal (
    id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    codigo VARCHAR(20) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    creado_en TIMESTAMP(6) NOT NULL,
    actualizado_en TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_core_sucursal PRIMARY KEY (id),
    CONSTRAINT fk_core_sucursal_empresa FOREIGN KEY (empresa_id) REFERENCES core_empresa(id) ON DELETE CASCADE,
    CONSTRAINT uq_core_sucursal_empresa_codigo UNIQUE (empresa_id, codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices para Sucursal
CREATE INDEX idx_core_sucursal_empresa ON core_sucursal (empresa_id);
CREATE INDEX idx_core_sucursal_empresa_estado ON core_sucursal (empresa_id, estado);
CREATE INDEX idx_core_sucursal_empresa_codigo ON core_sucursal (empresa_id, codigo);
