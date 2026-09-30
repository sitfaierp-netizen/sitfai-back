ALTER TABLE inventory_bodega_lote
    ADD COLUMN cantidad_reservada DECIMAL(19,4) NOT NULL DEFAULT 0.0000 AFTER cantidad;
