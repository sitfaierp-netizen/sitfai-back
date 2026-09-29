CREATE TABLE orders_pedido (
    id BINARY(16) PRIMARY KEY,
    empresa_id BINARY(16) NOT NULL,
    cliente_id BINARY(16) NOT NULL,
    total_monetario DECIMAL(19,2) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    version BIGINT NOT NULL,
    creado_en TIMESTAMP NOT NULL,
    creado_por VARCHAR(50) NOT NULL,
    actualizado_en TIMESTAMP,
    actualizado_por VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE orders_linea_pedido (
    id BINARY(16) PRIMARY KEY,
    pedido_id BINARY(16) NOT NULL,
    producto_id BINARY(16) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(19,2) NOT NULL,
    CONSTRAINT fk_orders_linea_pedido_pedido FOREIGN KEY (pedido_id) REFERENCES orders_pedido(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
