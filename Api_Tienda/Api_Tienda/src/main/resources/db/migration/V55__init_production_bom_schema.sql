CREATE TABLE lista_materiales (
    id UUID PRIMARY KEY,
    empresa_id UUID NOT NULL,
    producto_final_id UUID NOT NULL,
    estado VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX idx_lista_materiales_empresa ON lista_materiales(empresa_id);

CREATE TABLE componente_receta (
    id UUID PRIMARY KEY,
    receta_id UUID NOT NULL,
    insumo_id UUID NOT NULL,
    cantidad NUMERIC(19,4) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_componente_receta
        FOREIGN KEY (receta_id)
        REFERENCES lista_materiales (id)
        ON DELETE CASCADE
);

CREATE INDEX idx_componente_receta_id ON componente_receta(receta_id);
