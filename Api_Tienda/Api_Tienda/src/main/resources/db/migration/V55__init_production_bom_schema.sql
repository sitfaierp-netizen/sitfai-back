CREATE TABLE lista_materiales (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    empresa_id VARCHAR(36) NOT NULL,
    producto_final_id VARCHAR(36) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    creado_en TIMESTAMP(6) NOT NULL,
    creado_por VARCHAR(255) NOT NULL,
    actualizado_en TIMESTAMP(6) NULL,
    actualizado_por VARCHAR(255) NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_lista_materiales_empresa ON lista_materiales(empresa_id);

CREATE TABLE componente_receta (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    receta_id VARCHAR(36) NOT NULL,
    insumo_id VARCHAR(36) NOT NULL,
    cantidad DECIMAL(19,4) NOT NULL,
    creado_en TIMESTAMP(6) NOT NULL,
    creado_por VARCHAR(255) NOT NULL,
    actualizado_en TIMESTAMP(6) NULL,
    actualizado_por VARCHAR(255) NULL,
    CONSTRAINT fk_componente_receta
        FOREIGN KEY (receta_id)
        REFERENCES lista_materiales (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_componente_receta_id ON componente_receta(receta_id);
