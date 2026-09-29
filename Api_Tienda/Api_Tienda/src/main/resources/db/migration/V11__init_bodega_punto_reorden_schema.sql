-- ==============================================================================
-- Migración V11: Replenishment (Punto de Reorden)
-- ==============================================================================
-- Soporte para la Regla BOD-08: Puntos de reorden por producto.

CREATE TABLE inventory_bodega_punto_reorden (
    bodega_id VARCHAR(36) NOT NULL,
    producto_id VARCHAR(36) NOT NULL,
    punto_reorden DECIMAL(19, 4) NOT NULL,
    CONSTRAINT pk_inventory_bodega_punto_reorden PRIMARY KEY (bodega_id, producto_id),
    CONSTRAINT fk_inventory_bodega_punto_reorden_bodega FOREIGN KEY (bodega_id) 
        REFERENCES inventory_bodega (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices adicionales para rendimiento
CREATE INDEX idx_bodega_punto_reorden_producto ON inventory_bodega_punto_reorden (producto_id);
