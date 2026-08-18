-- SITFAI ERP — Migración V2: Esquema Inicial de Tienda / Ventas POS (Bounded Context: Api_Tienda)
-- Stack: MySQL 8.0+ / InnoDB / utf8mb4
-- Reglas aplicadas: REGLA-6 (snake_case, UUIDs VARCHAR(36)), MT-01 / MT-03 (empresa_id e índices multitenant en todas las tablas)

-- 1. Tabla: tienda_pedido (Aggregate Root Pedido)
CREATE TABLE IF NOT EXISTS tienda_pedido (
    id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    cliente_id VARCHAR(36) NOT NULL,
    estado VARCHAR(30) NOT NULL,
    total DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    moneda VARCHAR(3) NOT NULL DEFAULT 'USD',
    creado_en TIMESTAMP(6) NOT NULL,
    actualizado_en TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_tienda_pedido PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices Multitenant (MT-01, MT-03) y de Rendimiento
CREATE INDEX idx_tienda_pedido_empresa ON tienda_pedido (empresa_id);
CREATE INDEX idx_tienda_pedido_empresa_estado ON tienda_pedido (empresa_id, estado);
CREATE INDEX idx_tienda_pedido_cliente ON tienda_pedido (empresa_id, cliente_id);
CREATE INDEX idx_tienda_pedido_fecha ON tienda_pedido (creado_en);

-- 2. Tabla: tienda_linea_pedido (Líneas de Detalle del Pedido)
CREATE TABLE IF NOT EXISTS tienda_linea_pedido (
    id VARCHAR(36) NOT NULL,
    pedido_id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    producto_id VARCHAR(36) NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(19, 4) NOT NULL,
    moneda VARCHAR(3) NOT NULL DEFAULT 'USD',
    subtotal DECIMAL(19, 4) NOT NULL,
    CONSTRAINT pk_tienda_linea_pedido PRIMARY KEY (id),
    CONSTRAINT fk_tienda_linea_pedido FOREIGN KEY (pedido_id)
        REFERENCES tienda_pedido (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices Multitenant (MT-01, MT-03) e Integridad Referencial
CREATE INDEX idx_tienda_linea_pedido_empresa ON tienda_linea_pedido (empresa_id);
CREATE INDEX idx_tienda_linea_pedido_pedido ON tienda_linea_pedido (pedido_id);
CREATE INDEX idx_tienda_linea_pedido_producto ON tienda_linea_pedido (producto_id);
