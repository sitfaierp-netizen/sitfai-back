-- Migración V31: Restricción de unicidad global por Tenant para Bodegas
-- Reemplaza la restricción (empresa, sucursal, codigo) por (empresa, codigo) para cumplir Regla BOD-02

-- 1. Eliminar la restricción previa
ALTER TABLE inventory_bodega
    DROP INDEX uk_inventory_bodega_empresa_sucursal_codigo;

-- 2. Crear la nueva restricción estricta de unicidad por Empresa y Código
ALTER TABLE inventory_bodega
    ADD CONSTRAINT uk_inventory_bodega_empresa_codigo UNIQUE (empresa_id, codigo);
