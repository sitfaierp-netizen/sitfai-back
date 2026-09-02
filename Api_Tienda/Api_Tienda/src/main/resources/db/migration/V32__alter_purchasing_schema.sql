ALTER TABLE purchasing_orden_compra
    CHANGE COLUMN fecha_creacion creado_en DATETIME(6) NOT NULL,
    ADD COLUMN actualizado_en DATETIME(6) NULL,
    ADD COLUMN creado_por VARCHAR(36) NOT NULL DEFAULT 'system',
    ADD COLUMN actualizado_por VARCHAR(36) NULL,
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
