CREATE TABLE returns_autorizacion_devolucion (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    empresa_id VARCHAR(36) NOT NULL,
    documento_fuente_id VARCHAR(36) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_returns_rma_empresa_id UNIQUE (empresa_id, id)
);

CREATE TABLE returns_linea_devolucion (
    id VARCHAR(36) NOT NULL PRIMARY KEY,
    autorizacion_devolucion_id VARCHAR(36) NOT NULL,
    producto_id VARCHAR(36) NOT NULL,
    cantidad INT NOT NULL,
    motivo VARCHAR(255) NOT NULL,
    estado_inspeccion VARCHAR(20) NOT NULL,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT fk_returns_linea_rma FOREIGN KEY (autorizacion_devolucion_id) REFERENCES returns_autorizacion_devolucion(id) ON DELETE CASCADE
);

CREATE INDEX idx_returns_rma_empresa ON returns_autorizacion_devolucion(empresa_id);
CREATE INDEX idx_returns_rma_estado ON returns_autorizacion_devolucion(empresa_id, estado);
