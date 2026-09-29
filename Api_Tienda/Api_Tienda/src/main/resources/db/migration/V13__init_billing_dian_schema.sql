-- ---------------------------------------------------------
-- V13__init_billing_dian_schema.sql
-- Descripción: Tablas para el motor tributario DIAN (billing).
-- Reglas: MT-01 (aislamiento), Tipado DECIMAL(19, 4) exacto.
-- ---------------------------------------------------------

CREATE TABLE billing_resolucion (
    id VARCHAR(36) PRIMARY KEY,
    empresa_id VARCHAR(36) NOT NULL,
    prefijo VARCHAR(10) NOT NULL,
    rango_inicial BIGINT NOT NULL,
    rango_final BIGINT NOT NULL,
    vigencia_hasta DATE NOT NULL,
    activa BOOLEAN NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices de aislamiento y rendimiento (MT-01)
CREATE INDEX idx_resolucion_empresa ON billing_resolucion(empresa_id, activa);
