-- V43__init_outbound_logistics_schema.sql
-- Script de migracion para Outbound Logistics (Despacho de Mercancia)
-- MT-01: Aislamiento estricto via empresa_id
-- AUD-01: Campos de auditoria created_at / created_by

CREATE TABLE inventory_despacho (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    empresa_id VARCHAR(36) NOT NULL,
    pedido_id VARCHAR(36) NOT NULL,
    bodega_id VARCHAR(36),
    estado VARCHAR(30) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    creado_en TIMESTAMP NOT NULL,
    creado_por VARCHAR(255) NOT NULL,
    actualizado_en TIMESTAMP,
    actualizado_por VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_inv_despacho_empresa ON inventory_despacho(empresa_id);
CREATE INDEX idx_inv_despacho_pedido ON inventory_despacho(pedido_id);

CREATE TABLE inventory_linea_despacho (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    despacho_id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    producto_id VARCHAR(36) NOT NULL,
    cantidad DECIMAL(19,4) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    creado_en TIMESTAMP NOT NULL,
    creado_por VARCHAR(255) NOT NULL,
    actualizado_en TIMESTAMP,
    actualizado_por VARCHAR(255),
    CONSTRAINT fk_inv_linea_despacho 
        FOREIGN KEY (despacho_id) 
        REFERENCES inventory_despacho(id) 
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_inv_linea_despacho_despacho ON inventory_linea_despacho(despacho_id);
CREATE INDEX idx_inv_linea_despacho_empresa ON inventory_linea_despacho(empresa_id);
