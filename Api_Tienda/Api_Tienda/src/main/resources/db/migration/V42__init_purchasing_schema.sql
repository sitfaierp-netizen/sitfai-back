-- =============================================================================
-- SITFAI ERP — Migración V42: Esquema Multitenant y Auditoría para Purchasing
-- Bounded Context: purchasing (Puerto 8087)
-- Stack: MySQL 8.4 LTS / InnoDB / utf8mb4
-- Reglas: MT-01 (Aislamiento Multi-tenant), AUD-01 (Auditoría), BOD-04 (Trazabilidad)
-- =============================================================================

-- 1. Actualizar purchasing_orden_compra con fecha de emisión formal
ALTER TABLE purchasing_orden_compra
    ADD COLUMN emitido_en DATETIME(6) NULL;

-- 2. Asegurar columnas de auditoría completa (AUD-01) en purchasing_linea_orden
ALTER TABLE purchasing_linea_orden
    ADD COLUMN creado_en DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN creado_por VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN actualizado_en DATETIME(6) NULL,
    ADD COLUMN actualizado_por VARCHAR(100) NULL;

-- 3. Índices compuestos para aislamiento multitenant y filtrado por estado (MT-01, BOD-04)
CREATE INDEX idx_purchasing_oc_empresa_estado ON purchasing_orden_compra (empresa_id, estado);
