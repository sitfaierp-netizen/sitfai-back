-- ============================================================
-- V48__init_replenishment_schema.sql
-- Módulo: replenishment
-- Descripción: Tabla para las políticas de reposición automática de inventario.
-- Regla MT-01: empresa_id como clave de partición multi-tenant OBLIGATORIA.
-- Regla MT-03: UNIQUE constraint compuesto por empresa_id + bodega_id + producto_id.
-- Regla REGLA-6: Flyway gestiona las migraciones de esquema.
-- ============================================================

CREATE TABLE IF NOT EXISTS replenishment_politica_inventario (
    id               VARCHAR(36)  NOT NULL COMMENT 'UUID de la política (PK)',
    empresa_id       VARCHAR(36)  NOT NULL COMMENT 'Tenant discriminator — MT-01',
    bodega_id        VARCHAR(36)  NOT NULL COMMENT 'Bodega a la que aplica la política',
    producto_id      VARCHAR(36)  NOT NULL COMMENT 'Producto SKU monitoreado',
    punto_reorden    INT          NOT NULL COMMENT 'Umbral de alerta: si stock <= este valor se activa la reposición',
    nivel_optimo     INT          NOT NULL COMMENT 'Stock objetivo de reposición (debe ser > punto_reorden)',
    activa           TINYINT(1)   NOT NULL DEFAULT 1 COMMENT 'Soft-switch de la política',
    version          BIGINT       NOT NULL DEFAULT 0 COMMENT 'Concurrencia optimista (CON-01)',
    creado_en        DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    creado_por       VARCHAR(100) NOT NULL DEFAULT 'system',
    actualizado_en   DATETIME(6)  NULL,
    actualizado_por  VARCHAR(100) NULL,

    CONSTRAINT pk_replenishment_pol_inventario PRIMARY KEY (id),
    CONSTRAINT uk_replenishment_pol_tenant_bodega_producto
        UNIQUE (empresa_id, bodega_id, producto_id),
    CONSTRAINT chk_replenishment_pol_nivel_optimo
        CHECK (nivel_optimo > punto_reorden)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Políticas de reposición automática de inventario (módulo replenishment)';

-- Índices de consulta por tenant (MT-01)
CREATE INDEX idx_replenishment_pol_empresa
    ON replenishment_politica_inventario (empresa_id);

CREATE INDEX idx_replenishment_pol_empresa_bodega
    ON replenishment_politica_inventario (empresa_id, bodega_id);

CREATE INDEX idx_replenishment_pol_empresa_activa
    ON replenishment_politica_inventario (empresa_id, activa);
