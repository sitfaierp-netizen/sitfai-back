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
);

CREATE TABLE billing_factura (
    id VARCHAR(36) PRIMARY KEY,
    empresa_id VARCHAR(36) NOT NULL,
    nit_emisor VARCHAR(20) NOT NULL,
    nit_receptor VARCHAR(20) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    cufe VARCHAR(100),
    resolucion_dian VARCHAR(10) NOT NULL,
    subtotal DECIMAL(19, 4) NOT NULL,
    total_impuestos DECIMAL(19, 4) NOT NULL,
    total_general DECIMAL(19, 4) NOT NULL
);

CREATE TABLE billing_linea_factura (
    id VARCHAR(36) PRIMARY KEY,
    empresa_id VARCHAR(36) NOT NULL,
    factura_id VARCHAR(36) NOT NULL,
    concepto VARCHAR(255) NOT NULL,
    cantidad DECIMAL(19, 4) NOT NULL,
    precio_unitario DECIMAL(19, 4) NOT NULL,
    subtotal DECIMAL(19, 4) NOT NULL,
    total_impuestos DECIMAL(19, 4) NOT NULL,
    CONSTRAINT fk_linea_factura FOREIGN KEY (factura_id) REFERENCES billing_factura(id) ON DELETE CASCADE
);

-- Índices de aislamiento y rendimiento (MT-01)
CREATE INDEX idx_resolucion_empresa ON billing_resolucion(empresa_id, activa);
CREATE INDEX idx_factura_empresa_nit ON billing_factura(empresa_id, nit_receptor);
CREATE INDEX idx_linea_factura_empresa ON billing_linea_factura(empresa_id);
