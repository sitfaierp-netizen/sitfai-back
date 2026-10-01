-- Phase 0A: close tenant-integrity gaps left by the legacy fulfillment merge.
-- Existing rows are reconciled from their aggregate root before constraints are
-- enforced. V1-V58 remain immutable.

UPDATE fulfillment_linea_despacho linea
JOIN fulfillment_orden_despacho orden
  ON orden.id = linea.orden_despacho_id
SET linea.empresa_id = orden.empresa_id
WHERE linea.empresa_id IS NULL
   OR linea.empresa_id <> orden.empresa_id;

ALTER TABLE fulfillment_linea_despacho
    MODIFY COLUMN empresa_id VARCHAR(36) NOT NULL;

ALTER TABLE fulfillment_orden_despacho
    ADD CONSTRAINT uq_fulfillment_orden_empresa_id UNIQUE (empresa_id, id);

CREATE INDEX idx_fulfillment_linea_empresa_orden
    ON fulfillment_linea_despacho (empresa_id, orden_despacho_id);

ALTER TABLE fulfillment_linea_despacho
    ADD CONSTRAINT fk_fulfillment_linea_tenant_orden
        FOREIGN KEY (empresa_id, orden_despacho_id)
        REFERENCES fulfillment_orden_despacho (empresa_id, id)
        ON DELETE CASCADE;
