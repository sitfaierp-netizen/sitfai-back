-- SITFAI ERP — Migración V15: Esquema de Fulfillment (Despachos y WMS)
-- Stack: MySQL 8.0+ / InnoDB / utf8mb4
-- Reglas aplicadas: REGLA-6 (snake_case, UUIDs VARCHAR(36)), MT-01 (empresa_id e índices multitenant en todas las tablas)

-- 1. Tabla: fulfillment_orden_despacho (Aggregate Root)
CREATE TABLE IF NOT EXISTS fulfillment_orden_despacho (
    id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    pedido_origen_id VARCHAR(36) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    direccion_local VARCHAR(255) NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(20) NOT NULL,
    CONSTRAINT pk_fulfillment_orden_despacho PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices Multitenant (MT-01) y de Rendimiento
CREATE INDEX idx_fulfillment_orden_despacho_empresa ON fulfillment_orden_despacho (empresa_id);
CREATE INDEX idx_fulfillment_orden_despacho_origen ON fulfillment_orden_despacho (empresa_id, pedido_origen_id);

-- 2. Tabla: fulfillment_linea_despacho (Líneas de Detalle)
CREATE TABLE IF NOT EXISTS fulfillment_linea_despacho (
    id VARCHAR(36) NOT NULL,
    orden_despacho_id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    producto_id VARCHAR(36) NOT NULL,
    cantidad_solicitada DECIMAL(19, 4) NOT NULL,
    cantidad_preparada DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    CONSTRAINT pk_fulfillment_linea_despacho PRIMARY KEY (id),
    CONSTRAINT fk_fulfillment_linea_despacho FOREIGN KEY (orden_despacho_id)
        REFERENCES fulfillment_orden_despacho (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices Multitenant (MT-01)
CREATE INDEX idx_fulfillment_linea_despacho_empresa ON fulfillment_linea_despacho (empresa_id);
CREATE INDEX idx_fulfillment_linea_despacho_orden ON fulfillment_linea_despacho (orden_despacho_id);
CREATE INDEX idx_fulfillment_linea_despacho_producto ON fulfillment_linea_despacho (empresa_id, producto_id);
