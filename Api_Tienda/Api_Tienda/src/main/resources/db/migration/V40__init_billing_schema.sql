-- =============================================================================
-- SITFAI ERP — Migración V40: Esquema Multitenant y Trazabilidad Documental de Facturación
-- Bounded Context: billing (Puerto 8085)
-- Stack: MySQL 8.4 LTS / InnoDB / utf8mb4
-- Reglas: MT-01 (Aislamiento), AUD-01 (Auditoría), AUD-04 (Inmutabilidad/Anulación)
-- =============================================================================

-- 1. Actualizar billing_factura con trazabilidad de origen y anulación
ALTER TABLE billing_factura
    ADD COLUMN tipo_origen VARCHAR(30) NOT NULL DEFAULT 'ECOMMERCE',
    ADD COLUMN documento_fuente_id BINARY(16),
    ADD COLUMN motivo_anulacion VARCHAR(255),
    ADD COLUMN anulado_en DATETIME(6);

ALTER TABLE billing_factura
    MODIFY COLUMN ruc_cliente VARCHAR(30) NULL;

-- 2. Asegurar multitenancy estricto (MT-01), total y auditoría completa (AUD-01) en billing_linea_factura
ALTER TABLE billing_linea_factura
    ADD COLUMN empresa_id BINARY(16),
    ADD COLUMN total DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    ADD COLUMN creado_en DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN creado_por VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN actualizado_en DATETIME(6),
    ADD COLUMN actualizado_por VARCHAR(100);

-- 3. Índices de consulta multitenant y trazabilidad documental (MT-01, AUD-04)
CREATE INDEX idx_billing_factura_empresa_origen ON billing_factura(empresa_id, tipo_origen);
CREATE INDEX idx_billing_factura_doc_fuente ON billing_factura(empresa_id, documento_fuente_id);
CREATE INDEX idx_billing_linea_empresa ON billing_linea_factura(empresa_id);
