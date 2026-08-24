-- Migración V28: Agregar soporte para Soft Delete en entidades maestras
-- Entidades afectadas: Empresa, Sucursal, Bodega, Producto, Categoria, Proveedor, Usuario

-- 1. core_empresa
ALTER TABLE core_empresa
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN deleted_at DATETIME(6) NULL,
    ADD COLUMN deleted_by VARCHAR(36) NULL;

-- 2. core_sucursal
ALTER TABLE core_sucursal
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN deleted_at DATETIME(6) NULL,
    ADD COLUMN deleted_by VARCHAR(36) NULL;

-- 3. inventory_bodega
ALTER TABLE inventory_bodega
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN deleted_at DATETIME(6) NULL,
    ADD COLUMN deleted_by VARCHAR(36) NULL;

-- 4. catalog_productos
ALTER TABLE catalog_productos
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN deleted_at DATETIME(6) NULL,
    ADD COLUMN deleted_by VARCHAR(36) NULL;

-- 5. catalog_categorias
ALTER TABLE catalog_categorias
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN deleted_at DATETIME(6) NULL,
    ADD COLUMN deleted_by VARCHAR(36) NULL;

-- 6. sourcing_proveedores
ALTER TABLE sourcing_proveedores
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN deleted_at DATETIME(6) NULL,
    ADD COLUMN deleted_by VARCHAR(36) NULL;

-- 7. iam_usuario
ALTER TABLE iam_usuario
    ADD COLUMN activo BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN deleted_at DATETIME(6) NULL,
    ADD COLUMN deleted_by VARCHAR(36) NULL;

-- Para la estrategia de Unicidad (MySQL), lo ideal es delegar la validacion a nivel de Aplicacion
-- (Verificando activo=true en los Domain Services) o usando vistas. 
-- Aquí mantenemos los unique constraints actuales, pero en un entorno de produccion real con Soft Delete,
-- estas deberian ser removidas (DROP INDEX) y gestionadas mediante constraints compuestas 
-- (ej: UNIQUE(empresa_id, codigo, deleted_at) o similares).
