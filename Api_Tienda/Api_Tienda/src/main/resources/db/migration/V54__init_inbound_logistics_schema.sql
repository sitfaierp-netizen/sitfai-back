CREATE TABLE recepcion_mercancia (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL,
    orden_compra_id UUID NOT NULL,
    bodega_id UUID NOT NULL,
    estado VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_recepcion_empresa ON recepcion_mercancia(empresa_id);
CREATE INDEX idx_recepcion_orden_compra ON recepcion_mercancia(orden_compra_id);

CREATE TABLE linea_recepcion_mercancia (
    id UUID PRIMARY KEY,
    recepcion_id UUID NOT NULL,
    producto_id UUID NOT NULL,
    cantidad_esperada INT NOT NULL,
    cantidad_recibida INT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_linea_recepcion
        FOREIGN KEY (recepcion_id)
        REFERENCES recepcion_mercancia (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_linea_recepcion_recepcion_id ON linea_recepcion_mercancia(recepcion_id);
