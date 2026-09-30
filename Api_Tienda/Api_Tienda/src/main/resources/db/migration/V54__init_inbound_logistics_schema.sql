CREATE TABLE recepcion_mercancia (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    empresa_id VARCHAR(36) NOT NULL,
    orden_compra_id VARCHAR(36) NOT NULL,
    bodega_id VARCHAR(36) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    creado_en TIMESTAMP(6) NOT NULL,
    creado_por VARCHAR(255) NOT NULL,
    actualizado_en TIMESTAMP(6) NULL,
    actualizado_por VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_recepcion_empresa ON recepcion_mercancia(empresa_id);
CREATE INDEX idx_recepcion_orden_compra ON recepcion_mercancia(orden_compra_id);

CREATE TABLE linea_recepcion_mercancia (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    recepcion_id VARCHAR(36) NOT NULL,
    producto_id VARCHAR(36) NOT NULL,
    cantidad_esperada INT NOT NULL,
    cantidad_recibida INT NOT NULL,
    creado_en TIMESTAMP(6) NOT NULL,
    creado_por VARCHAR(255) NOT NULL,
    actualizado_en TIMESTAMP(6) NULL,
    actualizado_por VARCHAR(255) NULL,
    CONSTRAINT fk_linea_recepcion
        FOREIGN KEY (recepcion_id)
        REFERENCES recepcion_mercancia (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_linea_recepcion_recepcion_id ON linea_recepcion_mercancia(recepcion_id);
