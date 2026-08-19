-- =============================================================================
-- SITFAI ERP — Flyway V24
-- Módulo: catalog & sourcing
-- Correcciones de ADR-012 y ADR-013
-- =============================================================================

ALTER TABLE catalog_productos MODIFY precio_compra DECIMAL(19,4) NULL;
ALTER TABLE sourcing_proveedores ADD COLUMN plazo_entrega_dias INT NOT NULL DEFAULT 0;
