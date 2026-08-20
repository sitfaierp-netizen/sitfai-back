-- Script para inyectar columnas de auditoría a entidades maestras
-- NOTA: Algunas tablas ya podrían tener creado_en y actualizado_en.
-- Agregamos creado_por y actualizado_por para completar el esquema requerido por AuditableJpaEntity.

ALTER TABLE core_empresa
    ADD COLUMN creado_por VARCHAR(36) NOT NULL DEFAULT 'system',
    ADD COLUMN actualizado_por VARCHAR(36);

ALTER TABLE core_sucursal
    ADD COLUMN creado_por VARCHAR(36) NOT NULL DEFAULT 'system',
    ADD COLUMN actualizado_por VARCHAR(36);

ALTER TABLE inventory_bodega
    ADD COLUMN creado_por VARCHAR(36) NOT NULL DEFAULT 'system',
    ADD COLUMN actualizado_por VARCHAR(36);

ALTER TABLE catalog_productos
    ADD COLUMN creado_por VARCHAR(36) NOT NULL DEFAULT 'system',
    ADD COLUMN actualizado_por VARCHAR(36);

ALTER TABLE catalog_categorias
    ADD COLUMN creado_por VARCHAR(36) NOT NULL DEFAULT 'system',
    ADD COLUMN actualizado_por VARCHAR(36);

-- Asegurar que las columnas de timestamp existan si alguna tabla no las tenía (resolución de fallos de schema-validation)
-- Si ya existen, estas líneas podrían requerir ajuste según el motor (ej. IF NOT EXISTS). 
-- Para MySQL/H2 estándar (Flyway), si asume que fallaba catalog_categorias por faltar actualizado_en:
ALTER TABLE catalog_categorias
    ADD COLUMN actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
