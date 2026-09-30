-- V53 usó nombres de auditoría en inglés, mientras las entidades RMA heredan
-- el contrato en español de AuditableJpaEntity. Esta migración conserva los
-- valores existentes y alinea el esquema sin reescribir la migración aplicada.

ALTER TABLE returns_autorizacion_devolucion
    CHANGE COLUMN created_at creado_en TIMESTAMP(6) NULL,
    CHANGE COLUMN created_by creado_por VARCHAR(255) NULL,
    CHANGE COLUMN updated_at actualizado_en TIMESTAMP(6) NULL,
    CHANGE COLUMN updated_by actualizado_por VARCHAR(255) NULL;

UPDATE returns_autorizacion_devolucion
SET creado_en = COALESCE(creado_en, CURRENT_TIMESTAMP(6)),
    creado_por = COALESCE(creado_por, 'flyway-v57-legacy');

ALTER TABLE returns_autorizacion_devolucion
    MODIFY COLUMN creado_en TIMESTAMP(6) NOT NULL,
    MODIFY COLUMN creado_por VARCHAR(255) NOT NULL;

ALTER TABLE returns_linea_devolucion
    CHANGE COLUMN created_at creado_en TIMESTAMP(6) NULL,
    CHANGE COLUMN created_by creado_por VARCHAR(255) NULL,
    CHANGE COLUMN updated_at actualizado_en TIMESTAMP(6) NULL,
    CHANGE COLUMN updated_by actualizado_por VARCHAR(255) NULL;

UPDATE returns_linea_devolucion
SET creado_en = COALESCE(creado_en, CURRENT_TIMESTAMP(6)),
    creado_por = COALESCE(creado_por, 'flyway-v57-legacy');

ALTER TABLE returns_linea_devolucion
    MODIFY COLUMN creado_en TIMESTAMP(6) NOT NULL,
    MODIFY COLUMN creado_por VARCHAR(255) NOT NULL;
