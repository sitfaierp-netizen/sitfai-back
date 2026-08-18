-- SITFAI ERP — Migración V1: Esquema Inicial de Inventario (Bounded Context: inventory)
-- Stack: MySQL 8.0+ / InnoDB / utf8mb4
-- Reglas aplicadas: REGLA-6 (snake_case, UUIDs), MT-01/MT-03 (empresa_id en todas las tablas), BOD-01 a BOD-05

-- 1. Tabla: inventory_bodega (Aggregate Root)
CREATE TABLE IF NOT EXISTS inventory_bodega (
    id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    sucursal_id VARCHAR(36) NOT NULL,
    codigo VARCHAR(50) NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    activa BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en TIMESTAMP(6) NOT NULL,
    actualizado_en TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_inventory_bodega PRIMARY KEY (id),
    CONSTRAINT uk_inventory_bodega_empresa_sucursal_codigo UNIQUE (empresa_id, sucursal_id, codigo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_inventory_bodega_empresa ON inventory_bodega (empresa_id);
CREATE INDEX idx_inventory_bodega_sucursal ON inventory_bodega (empresa_id, sucursal_id);

-- 2. Tabla: inventory_bodega_stock (ElementCollection de Stock por Producto)
CREATE TABLE IF NOT EXISTS inventory_bodega_stock (
    bodega_id VARCHAR(36) NOT NULL,
    producto_id VARCHAR(36) NOT NULL,
    cantidad DECIMAL(19, 4) NOT NULL DEFAULT 0.0000,
    CONSTRAINT pk_inventory_bodega_stock PRIMARY KEY (bodega_id, producto_id),
    CONSTRAINT fk_inventory_stock_bodega FOREIGN KEY (bodega_id) 
        REFERENCES inventory_bodega (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_inventory_stock_producto ON inventory_bodega_stock (producto_id);

-- 3. Tabla: inventory_movimiento (Historial Inmutable de Movimientos de Stock)
CREATE TABLE IF NOT EXISTS inventory_movimiento (
    id VARCHAR(36) NOT NULL,
    bodega_id VARCHAR(36) NOT NULL,
    producto_id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    cantidad DECIMAL(19, 4) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    doc_fuente_tipo VARCHAR(50) NOT NULL,
    doc_fuente_numero VARCHAR(100) NOT NULL,
    fecha_registro TIMESTAMP(6) NOT NULL,
    CONSTRAINT pk_inventory_movimiento PRIMARY KEY (id),
    CONSTRAINT fk_inventory_movimiento_bodega FOREIGN KEY (bodega_id) 
        REFERENCES inventory_bodega (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_inventory_movimiento_empresa ON inventory_movimiento (empresa_id);
CREATE INDEX idx_inventory_movimiento_bodega_producto ON inventory_movimiento (bodega_id, producto_id);
CREATE INDEX idx_inventory_movimiento_fecha ON inventory_movimiento (fecha_registro);
