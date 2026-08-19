CREATE TABLE inventory_bodega_lote (
    id VARCHAR(36) NOT NULL,
    bodega_id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    producto_id VARCHAR(36) NOT NULL,
    lote_id VARCHAR(100) NOT NULL,
    cantidad DECIMAL(19,4) NOT NULL,
    fecha_caducidad DATETIME(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_inv_bodega_lote_bodega FOREIGN KEY (bodega_id) REFERENCES inventory_bodega (id) ON DELETE CASCADE,
    CONSTRAINT chk_inv_bodega_lote_cantidad CHECK (cantidad >= 0),
    CONSTRAINT uk_inv_bodega_lote_producto_lote UNIQUE (bodega_id, producto_id, lote_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_inv_bodega_lote_empresa ON inventory_bodega_lote(empresa_id);
CREATE INDEX idx_inv_bodega_lote_producto ON inventory_bodega_lote(producto_id);

-- Migración segura de datos existentes (si aplica)
-- Convertir los registros de inventory_bodega_stock hacia inventory_bodega_lote con un lote por defecto ('LEGACY')
INSERT INTO inventory_bodega_lote (id, bodega_id, empresa_id, producto_id, lote_id, cantidad, fecha_caducidad)
SELECT 
    UUID(), 
    b.id, 
    b.empresa_id, 
    s.producto_id, 
    'LEGACY', 
    s.cantidad, 
    NULL
FROM inventory_bodega_stock s
JOIN inventory_bodega b ON s.bodega_id = b.id;

-- Una vez migrados de forma segura, podemos prescindir de la tabla antigua.
-- Sin embargo, como política de no pérdida, la renombramos en lugar de hacer DROP
RENAME TABLE inventory_bodega_stock TO inventory_bodega_stock_deprecated;

-- Alterar tabla de movimientos para añadir el lote
ALTER TABLE inventory_movimiento ADD COLUMN lote_id VARCHAR(100) NULL;
