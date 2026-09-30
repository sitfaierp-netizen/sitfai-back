CREATE TABLE orden_produccion (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    empresa_id VARCHAR(36) NOT NULL,
    receta_id VARCHAR(36) NOT NULL,
    bodega_id VARCHAR(36) NOT NULL,
    cantidad_producir INT NOT NULL,
    estado VARCHAR(50) NOT NULL,
    creado_en TIMESTAMP(6) NOT NULL,
    creado_por VARCHAR(255) NOT NULL,
    actualizado_en TIMESTAMP(6) NULL,
    actualizado_por VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_orden_produccion_empresa ON orden_produccion(empresa_id);
