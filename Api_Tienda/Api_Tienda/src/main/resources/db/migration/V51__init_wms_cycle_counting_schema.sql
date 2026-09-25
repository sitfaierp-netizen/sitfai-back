-- ============================================================
-- V51__init_wms_cycle_counting_schema.sql
-- Módulo: inventory (WMS Logistics Intelligence - Cycle Counting)
-- Descripción: Tablas para el ciclo de conteo cíclico y auditoría física de inventario.
-- Regla MT-01: empresa_id como clave de partición multi-tenant OBLIGATORIA.
-- Regla AUD-01: Campos de auditoría created_at/by y updated_at/by.
-- ============================================================

CREATE TABLE IF NOT EXISTS inventory_conteo_ciclico (
    id                  VARCHAR(36)    NOT NULL COMMENT 'UUID del Conteo Cíclico (PK)',
    empresa_id          VARCHAR(36)    NOT NULL COMMENT 'Tenant discriminator — MT-01',
    bodega_id           VARCHAR(36)    NOT NULL COMMENT 'Bodega donde se realiza la auditoría',
    fecha_programada    DATE           NOT NULL COMMENT 'Fecha prevista para la ejecución del conteo',
    estado              VARCHAR(30)    NOT NULL COMMENT 'PLANIFICADO, EN_EJECUCION, COMPLETADO, CON_DISCREPANCIAS',
    version             BIGINT         NOT NULL DEFAULT 0 COMMENT 'Concurrencia optimista (CON-01)',
    creado_en           DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    creado_por          VARCHAR(100)   NOT NULL DEFAULT 'system',
    actualizado_en      DATETIME(6)    NULL,
    actualizado_por     VARCHAR(100)   NULL,

    CONSTRAINT pk_inventory_conteo_ciclico PRIMARY KEY (id),
    CONSTRAINT uk_inv_conteo_empresa_id UNIQUE (empresa_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Cabecera de Conteo Cíclico de Inventario (WMS Intelligence)';

CREATE INDEX idx_inv_conteo_empresa
    ON inventory_conteo_ciclico (empresa_id);

CREATE INDEX idx_inv_conteo_empresa_bodega
    ON inventory_conteo_ciclico (empresa_id, bodega_id);

CREATE INDEX idx_inv_conteo_empresa_estado
    ON inventory_conteo_ciclico (empresa_id, estado);

CREATE TABLE IF NOT EXISTS inventory_detalle_conteo (
    id                  VARCHAR(36)    NOT NULL COMMENT 'UUID de la línea de detalle (PK)',
    empresa_id          VARCHAR(36)    NOT NULL COMMENT 'Tenant discriminator — MT-01',
    conteo_id           VARCHAR(36)    NOT NULL COMMENT 'FK hacia inventory_conteo_ciclico',
    producto_id         VARCHAR(36)    NOT NULL COMMENT 'Producto SKU auditado',
    cantidad_teorica    INT            NOT NULL DEFAULT 0 COMMENT 'Cantidad registrada en el sistema',
    cantidad_fisica     INT            NULL COMMENT 'Cantidad física contada en estantería',
    version             BIGINT         NOT NULL DEFAULT 0 COMMENT 'Concurrencia optimista (CON-01)',
    creado_en           DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    creado_por          VARCHAR(100)   NOT NULL DEFAULT 'system',
    actualizado_en      DATETIME(6)    NULL,
    actualizado_por     VARCHAR(100)   NULL,

    CONSTRAINT pk_inventory_detalle_conteo PRIMARY KEY (id),
    CONSTRAINT uk_inv_det_conteo_prod UNIQUE (empresa_id, conteo_id, producto_id),
    CONSTRAINT fk_inv_det_conteo_padre
        FOREIGN KEY (conteo_id) REFERENCES inventory_conteo_ciclico (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Detalle de líneas y conteos físicos por SKU (WMS Intelligence)';

CREATE INDEX idx_inv_det_conteo_empresa
    ON inventory_detalle_conteo (empresa_id);

CREATE INDEX idx_inv_det_conteo_padre
    ON inventory_detalle_conteo (empresa_id, conteo_id);

CREATE INDEX idx_inv_det_conteo_prod
    ON inventory_detalle_conteo (empresa_id, producto_id);
