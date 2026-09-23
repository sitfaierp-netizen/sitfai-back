-- V44__init_pos_schema.sql
-- SITFAI ERP — Migración V44: Esquema y Auditoría Transaccional para POS (Turnos y Cajas)
-- MT-01: Aislamiento multitenant via empresa_id
-- AUD-01: Auditoría transaccional con creado_en, creado_por, actualizado_en, actualizado_por (AuditableJpaEntity)
-- MONEY-01: Precisión monetaria en DECIMAL(19,4)

-- 1. Modificar pos_turno_caja para añadir columnas de arqueo y auditoría requeridas por JPA
ALTER TABLE pos_turno_caja
    MODIFY COLUMN sucursal_id VARCHAR(36) NULL,
    MODIFY COLUMN usuario_id VARCHAR(36) NULL,
    ADD COLUMN cajero_id VARCHAR(36) NULL,
    ADD COLUMN monto_cierre DECIMAL(19,4) NULL,
    ADD COLUMN total_teorico DECIMAL(19,4) NULL,
    ADD COLUMN descuadre DECIMAL(19,4) NULL,
    ADD COLUMN fecha_apertura TIMESTAMP NULL,
    ADD COLUMN fecha_cierre TIMESTAMP NULL,
    ADD COLUMN creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN creado_por VARCHAR(255) NOT NULL DEFAULT 'SYSTEM_POS',
    ADD COLUMN actualizado_en TIMESTAMP NULL DEFAULT NULL,
    ADD COLUMN actualizado_por VARCHAR(255) NULL;

-- 2. Modificar pos_transaccion_caja para añadir columnas de documento fuente y auditoría
ALTER TABLE pos_transaccion_caja
    ADD COLUMN documento_fuente_id VARCHAR(100) NULL,
    ADD COLUMN fecha_hora TIMESTAMP NULL,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN creado_por VARCHAR(255) NOT NULL DEFAULT 'SYSTEM_POS',
    ADD COLUMN actualizado_en TIMESTAMP NULL DEFAULT NULL,
    ADD COLUMN actualizado_por VARCHAR(255) NULL;

-- 3. Índices de consulta para optimizar búsquedas por tenant y caja
CREATE INDEX idx_pos_turno_cajero ON pos_turno_caja (empresa_id, cajero_id);
CREATE INDEX idx_pos_tx_fuente ON pos_transaccion_caja (empresa_id, documento_fuente_id);
