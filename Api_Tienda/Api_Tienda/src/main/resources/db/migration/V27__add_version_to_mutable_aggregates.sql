-- CORE-INTEGRITY-02: Concurrencia Optimista
-- Agregar columna version a los Aggregate Roots mutables para controlar la concurrencia.

ALTER TABLE inventory_bodega ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE core_empresa ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
