-- V36__init_inbound_recepcion.sql
-- Script de migracion para el Inbound Logistics (Recepcion de Mercancia)
-- MT-01: Aislamiento estricto via empresa_id

CREATE TABLE inventory_recepcion (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    empresa_id VARCHAR(36) NOT NULL,
    bodega_destino_id VARCHAR(36) NOT NULL,
    orden_compra_origen_id VARCHAR(36) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    creado_en TIMESTAMP NOT NULL,
    creado_por VARCHAR(255) NOT NULL,
    actualizado_en TIMESTAMP,
    actualizado_por VARCHAR(255)
);

-- Indexing for multi-tenant isolation
CREATE INDEX idx_inventory_recepcion_empresa ON inventory_recepcion(empresa_id);
CREATE INDEX idx_inventory_recepcion_bodega ON inventory_recepcion(bodega_destino_id);

CREATE TABLE inventory_linea_recepcion (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    recepcion_id VARCHAR(36) NOT NULL,
    producto_id VARCHAR(36) NOT NULL,
    cantidad_recibida DECIMAL(19,4) NOT NULL,
    codigo_lote VARCHAR(50) NOT NULL,
    fecha_caducidad DATE NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    creado_en TIMESTAMP NOT NULL,
    creado_por VARCHAR(255) NOT NULL,
    actualizado_en TIMESTAMP,
    actualizado_por VARCHAR(255),
    CONSTRAINT fk_inv_linea_recepcion 
        FOREIGN KEY (recepcion_id) 
        REFERENCES inventory_recepcion(id) 
        ON DELETE CASCADE
);

CREATE INDEX idx_inventory_linea_recepcion_recepcion ON inventory_linea_recepcion(recepcion_id);
