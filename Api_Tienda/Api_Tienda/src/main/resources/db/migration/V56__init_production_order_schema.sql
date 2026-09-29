CREATE TABLE orden_produccion (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL,
    receta_id UUID NOT NULL,
    bodega_id UUID NOT NULL,
    cantidad_producir INT NOT NULL,
    estado VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_orden_produccion_empresa ON orden_produccion(empresa_id);
