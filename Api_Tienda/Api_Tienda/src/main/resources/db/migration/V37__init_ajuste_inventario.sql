-- V37__init_ajuste_inventario.sql
-- Descripción: Tablas para gestionar los ajustes de inventario (Cycle Counting)

CREATE TABLE inv_ajustes_inventario (
    id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    bodega_id VARCHAR(36) NOT NULL,
    motivo VARCHAR(50) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    creado_en TIMESTAMP(6) NOT NULL,
    creado_por VARCHAR(255) NOT NULL,
    actualizado_en TIMESTAMP(6),
    actualizado_por VARCHAR(255),
    version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE inv_lineas_ajuste (
    id VARCHAR(36) NOT NULL,
    ajuste_id VARCHAR(36) NOT NULL,
    producto_id VARCHAR(36) NOT NULL,
    codigo_lote VARCHAR(100),
    fecha_caducidad DATE,
    diferencia DECIMAL(19,4) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_inv_lineas_ajuste_ajuste FOREIGN KEY (ajuste_id) REFERENCES inv_ajustes_inventario(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
