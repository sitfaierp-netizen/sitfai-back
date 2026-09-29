-- ---------------------------------------------------------
-- V20__init_nota_credito_schema.sql
-- Descripción: Tablas para Nota de Crédito (DIAN) (billing).
-- ---------------------------------------------------------

CREATE TABLE billing_nota_credito (
    id VARCHAR(36) PRIMARY KEY,
    empresa_id VARCHAR(36) NOT NULL,
    factura_afectada_id VARCHAR(36) NOT NULL,
    cufe_factura_afectada VARCHAR(100) NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    cufe VARCHAR(100),
    subtotal DECIMAL(19, 4) NOT NULL,
    total_impuestos DECIMAL(19, 4) NOT NULL,
    total_general DECIMAL(19, 4) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE billing_linea_nota_credito (
    id VARCHAR(36) PRIMARY KEY,
    empresa_id VARCHAR(36) NOT NULL,
    nota_credito_id VARCHAR(36) NOT NULL,
    concepto VARCHAR(255) NOT NULL,
    cantidad DECIMAL(19, 4) NOT NULL,
    precio_unitario DECIMAL(19, 4) NOT NULL,
    subtotal DECIMAL(19, 4) NOT NULL,
    total_impuestos DECIMAL(19, 4) NOT NULL,
    CONSTRAINT fk_linea_nota_credito FOREIGN KEY (nota_credito_id) REFERENCES billing_nota_credito(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_nota_credito_empresa_factura ON billing_nota_credito(empresa_id, factura_afectada_id);
