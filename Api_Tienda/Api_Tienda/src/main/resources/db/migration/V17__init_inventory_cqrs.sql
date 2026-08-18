-- ---------------------------------------------------------
-- V17__init_inventory_cqrs.sql
-- Descripción: Creación de la vista desnormalizada (tabla real)
-- para el modelo de lectura (CQRS) del módulo Inventory.
-- Reglas: MT-01 (aislamiento por empresa_id).
-- ---------------------------------------------------------

CREATE TABLE inventory_stock_view (
    empresa_id BINARY(16) NOT NULL,
    bodega_id BINARY(16) NOT NULL,
    producto_id BINARY(16) NOT NULL,
    cantidad_total DECIMAL(19, 4) NOT NULL,
    ultima_actualizacion TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (empresa_id, bodega_id, producto_id)
);

-- Índice compuesto para acelerar las consultas del modelo de lectura
CREATE INDEX idx_inv_view_empresa_bodega ON inventory_stock_view (empresa_id, bodega_id);
