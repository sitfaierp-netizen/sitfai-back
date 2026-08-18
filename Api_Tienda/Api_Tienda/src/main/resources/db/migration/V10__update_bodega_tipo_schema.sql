-- V10: Añadir soporte para Logística Inversa (Tipo de Bodega)
-- Aplica a la tabla principal del agregado inventory_bodega

ALTER TABLE inventory_bodega 
ADD COLUMN tipo VARCHAR(30) NOT NULL DEFAULT 'VENTA';
