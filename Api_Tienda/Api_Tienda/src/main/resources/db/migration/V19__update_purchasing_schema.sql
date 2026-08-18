-- =====================================================================
-- SITFAI ERP - Migración Flyway (Purchasing Composite Index MT-01)
-- =====================================================================

-- Verificación de V14 previa e indexado compuesto para rendimiento Multitenant
-- Si la tabla existe (creada en V14), simplemente agregamos el índice compuesto.

CREATE INDEX idx_purchasing_oc_empresa_prov 
ON purchasing_orden_compra (empresa_id, proveedor_id);
