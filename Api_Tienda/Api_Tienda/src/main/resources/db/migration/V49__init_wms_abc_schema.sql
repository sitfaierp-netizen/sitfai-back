-- ============================================================
-- V49__init_wms_abc_schema.sql
-- Módulo: inventory (WMS Logistics Intelligence)
-- Descripción: Tabla para la clasificación ABC de productos por bodega.
-- Regla MT-01: empresa_id como clave de partición multi-tenant OBLIGATORIA.
-- Regla MT-03: UNIQUE constraint compuesto por empresa_id + bodega_id + producto_id.
-- Regla MONEY-01: valor_total_despachado persistido con DECIMAL(19,4).
-- Regla AUD-01: Campos de auditoría created_at/by y updated_at/by.
-- ============================================================

CREATE TABLE IF NOT EXISTS inventory_clasificacion_producto (
    id                      VARCHAR(36)    NOT NULL COMMENT 'UUID del Análisis (PK)',
    empresa_id              VARCHAR(36)    NOT NULL COMMENT 'Tenant discriminator — MT-01',
    bodega_id               VARCHAR(36)    NOT NULL COMMENT 'Bodega de análisis',
    producto_id             VARCHAR(36)    NOT NULL COMMENT 'Producto SKU clasificado',
    categoria               VARCHAR(20)    NOT NULL COMMENT 'Categoría ABC (A, B, C, NO_CLASIFICADO)',
    frecuencia_salida       INT            NOT NULL DEFAULT 0 COMMENT 'Frecuencia de salida en el período',
    valor_total_despachado  DECIMAL(19, 4) NOT NULL DEFAULT 0.0000 COMMENT 'Valor monetario total despachado (MONEY-01)',
    version                 BIGINT         NOT NULL DEFAULT 0 COMMENT 'Concurrencia optimista (CON-01)',
    creado_en               DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    creado_por              VARCHAR(100)   NOT NULL DEFAULT 'system',
    actualizado_en          DATETIME(6)    NULL,
    actualizado_por         VARCHAR(100)   NULL,

    CONSTRAINT pk_inventory_clasificacion_producto PRIMARY KEY (id),
    CONSTRAINT uk_inv_clasif_empresa_bodega_prod
        UNIQUE (empresa_id, bodega_id, producto_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Clasificación ABC de inventario (WMS Logistics Intelligence)';

-- Índices para acelerar búsquedas por tenant, bodega, categoría y producto (MT-01)
CREATE INDEX idx_inv_clasif_empresa
    ON inventory_clasificacion_producto (empresa_id);

CREATE INDEX idx_inv_clasif_empresa_bodega
    ON inventory_clasificacion_producto (empresa_id, bodega_id);

CREATE INDEX idx_inv_clasif_empresa_categoria
    ON inventory_clasificacion_producto (empresa_id, categoria);

CREATE INDEX idx_inv_clasif_empresa_producto
    ON inventory_clasificacion_producto (empresa_id, producto_id);
