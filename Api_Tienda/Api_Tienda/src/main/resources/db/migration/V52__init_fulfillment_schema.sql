-- V15 creó la primera versión de Fulfillment. V52 evoluciona esas tablas al
-- modelo canónico actual sin recrearlas ni descartar datos históricos.

ALTER TABLE fulfillment_orden_despacho
    CHANGE COLUMN pedido_origen_id pedido_id VARCHAR(36) NOT NULL,
    ADD COLUMN bodega_id VARCHAR(36) NULL AFTER pedido_id,
    ADD COLUMN creado_en TIMESTAMP(6) NULL,
    ADD COLUMN creado_por VARCHAR(255) NULL,
    ADD COLUMN actualizado_en TIMESTAMP(6) NULL,
    ADD COLUMN actualizado_por VARCHAR(255) NULL,
    MODIFY COLUMN estado VARCHAR(30) NOT NULL,
    MODIFY COLUMN direccion_local VARCHAR(255) NULL,
    MODIFY COLUMN ciudad VARCHAR(100) NULL,
    MODIFY COLUMN codigo_postal VARCHAR(20) NULL;

-- Los registros de V15 no almacenaban bodega ni auditoría. Se usa un UUID
-- centinela válido para señalar explícitamente ese dato histórico desconocido.
UPDATE fulfillment_orden_despacho
SET bodega_id = '00000000-0000-0000-0000-000000000000',
    creado_en = CURRENT_TIMESTAMP(6),
    creado_por = 'flyway-v52-legacy'
WHERE bodega_id IS NULL;

ALTER TABLE fulfillment_orden_despacho
    MODIFY COLUMN bodega_id VARCHAR(36) NOT NULL,
    MODIFY COLUMN creado_en TIMESTAMP(6) NOT NULL,
    MODIFY COLUMN creado_por VARCHAR(255) NOT NULL;

ALTER TABLE fulfillment_linea_despacho
    ADD COLUMN cantidad INT NULL AFTER producto_id,
    ADD COLUMN creado_en TIMESTAMP(6) NULL,
    ADD COLUMN creado_por VARCHAR(255) NULL,
    ADD COLUMN actualizado_en TIMESTAMP(6) NULL,
    ADD COLUMN actualizado_por VARCHAR(255) NULL,
    MODIFY COLUMN empresa_id VARCHAR(36) NULL,
    MODIFY COLUMN cantidad_solicitada DECIMAL(19, 4) NULL;

-- La cantidad original se conserva en cantidad_solicitada; la nueva columna
-- entera materializa el contrato del agregado actual.
UPDATE fulfillment_linea_despacho
SET cantidad = CAST(cantidad_solicitada AS UNSIGNED),
    creado_en = CURRENT_TIMESTAMP(6),
    creado_por = 'flyway-v52-legacy'
WHERE cantidad IS NULL;

ALTER TABLE fulfillment_linea_despacho
    MODIFY COLUMN cantidad INT NOT NULL,
    MODIFY COLUMN creado_en TIMESTAMP(6) NOT NULL,
    MODIFY COLUMN creado_por VARCHAR(255) NOT NULL;

CREATE INDEX idx_fulfillment_despacho_empresa_estado
    ON fulfillment_orden_despacho (empresa_id, estado);
