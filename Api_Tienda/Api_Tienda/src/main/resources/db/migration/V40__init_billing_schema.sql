-- =============================================================================
-- SITFAI ERP — Migración V40: Esquema Multitenant y Trazabilidad Documental de Facturación
-- Bounded Context: billing (Puerto 8085)
-- Stack: MySQL 8.4 LTS / InnoDB / utf8mb4
-- Reglas: MT-01 (Aislamiento), AUD-01 (Auditoría), AUD-04 (Inmutabilidad/Anulación)
-- =============================================================================

CREATE TABLE IF NOT EXISTS billing_factura (
    id BINARY(16) NOT NULL,
    empresa_id BINARY(16) NOT NULL,
    cliente_id BINARY(16) NOT NULL,
    pedido_id BINARY(16),
    tipo_origen VARCHAR(30) DEFAULT 'ECOMMERCE',
    documento_fuente_id BINARY(16),
    ruc_cliente VARCHAR(30),
    subtotal DECIMAL(19,4) NOT NULL,
    total_impuestos DECIMAL(19,4) NOT NULL,
    total_general DECIMAL(19,4) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    motivo_anulacion VARCHAR(255),
    anulado_en DATETIME(6),
    version BIGINT NOT NULL DEFAULT 0,
    creado_en DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    creado_por VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    actualizado_en DATETIME(6),
    actualizado_por VARCHAR(100),
    CONSTRAINT pk_billing_factura PRIMARY KEY (id),
    CONSTRAINT uq_billing_factura_empresa_id_uuid UNIQUE (empresa_id, id)
);

CREATE TABLE IF NOT EXISTS billing_linea_factura (
    id BINARY(16) NOT NULL,
    factura_id BINARY(16) NOT NULL,
    empresa_id BINARY(16) NOT NULL,
    concepto VARCHAR(255) NOT NULL,
    cantidad DECIMAL(19,4) NOT NULL,
    precio_unitario DECIMAL(19,4) NOT NULL,
    subtotal DECIMAL(19,4) NOT NULL,
    total_impuestos DECIMAL(19,4) NOT NULL,
    total DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    creado_en DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    creado_por VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    actualizado_en DATETIME(6),
    actualizado_por VARCHAR(100),
    CONSTRAINT pk_billing_linea_factura PRIMARY KEY (id),
    CONSTRAINT fk_billing_linea_factura_factura FOREIGN KEY (factura_id) REFERENCES billing_factura (id) ON DELETE CASCADE
);

-- Actualización condicional por si las tablas fueron pre-creadas en migraciones anteriores
ALTER TABLE billing_factura
    ADD COLUMN IF NOT EXISTS tipo_origen VARCHAR(30) DEFAULT 'ECOMMERCE',
    ADD COLUMN IF NOT EXISTS documento_fuente_id BINARY(16),
    ADD COLUMN IF NOT EXISTS motivo_anulacion VARCHAR(255),
    ADD COLUMN IF NOT EXISTS anulado_en DATETIME(6);

ALTER TABLE billing_factura
    MODIFY COLUMN ruc_cliente VARCHAR(30) NULL;

ALTER TABLE billing_linea_factura
    ADD COLUMN IF NOT EXISTS empresa_id BINARY(16),
    ADD COLUMN IF NOT EXISTS total DECIMAL(19,4) NOT NULL DEFAULT 0.0000,
    ADD COLUMN IF NOT EXISTS creado_en DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN IF NOT EXISTS creado_por VARCHAR(100) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN IF NOT EXISTS actualizado_en DATETIME(6),
    ADD COLUMN IF NOT EXISTS actualizado_por VARCHAR(100);

-- Índices de consulta multitenant y trazabilidad documental (MT-01, AUD-04)
CREATE INDEX idx_billing_factura_empresa ON billing_factura(empresa_id);
CREATE INDEX idx_billing_factura_origen ON billing_factura(empresa_id, documento_fuente_id);
CREATE INDEX idx_billing_linea_empresa ON billing_linea_factura(empresa_id);
