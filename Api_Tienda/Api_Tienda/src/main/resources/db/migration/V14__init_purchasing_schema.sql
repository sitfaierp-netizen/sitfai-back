CREATE TABLE purchasing_orden_compra (
    id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    proveedor_id VARCHAR(36) NOT NULL,
    fecha_creacion DATETIME(6) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    costo_total_calculado DECIMAL(19,4) NOT NULL,
    CONSTRAINT pk_purchasing_orden_compra PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_purchasing_oc_empresa ON purchasing_orden_compra (empresa_id);

CREATE TABLE purchasing_linea_orden (
    id VARCHAR(36) NOT NULL,
    orden_compra_id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    producto_id VARCHAR(36) NOT NULL,
    cantidad_solicitada DECIMAL(19,4) NOT NULL,
    costo_unitario_esperado DECIMAL(19,4) NOT NULL,
    subtotal DECIMAL(19,4) NOT NULL,
    CONSTRAINT pk_purchasing_linea_orden PRIMARY KEY (id),
    CONSTRAINT fk_purchasing_linea_oc FOREIGN KEY (orden_compra_id) REFERENCES purchasing_orden_compra (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_purchasing_lo_empresa ON purchasing_linea_orden (empresa_id);
