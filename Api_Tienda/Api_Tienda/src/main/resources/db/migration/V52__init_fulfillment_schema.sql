CREATE TABLE fulfillment_orden_despacho (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    empresa_id VARCHAR(36) NOT NULL,
    pedido_id VARCHAR(36) NOT NULL,
    bodega_id VARCHAR(36) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_fulfillment_despacho_empresa_id UNIQUE (empresa_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE fulfillment_linea_despacho (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    orden_despacho_id VARCHAR(36) NOT NULL,
    producto_id VARCHAR(36) NOT NULL,
    cantidad INT NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_linea_orden_despacho FOREIGN KEY (orden_despacho_id) REFERENCES fulfillment_orden_despacho(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_fulfillment_despacho_empresa ON fulfillment_orden_despacho(empresa_id);
CREATE INDEX idx_fulfillment_despacho_empresa_estado ON fulfillment_orden_despacho(empresa_id, estado);
