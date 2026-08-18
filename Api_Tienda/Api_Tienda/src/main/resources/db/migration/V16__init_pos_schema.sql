-- SITFAI ERP — Migración V16: Esquema de POS (Punto de Venta)
-- Stack: MySQL 8.0+ / InnoDB / utf8mb4
-- Reglas aplicadas: REGLA-6 (snake_case, UUIDs VARCHAR(36)), MT-01 (empresa_id e índices multitenant en todas las tablas)

-- 1. Tabla: pos_turno_caja (Aggregate Root)
CREATE TABLE IF NOT EXISTS pos_turno_caja (
    id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    caja_id VARCHAR(36) NOT NULL,
    sucursal_id VARCHAR(36) NOT NULL,
    usuario_id VARCHAR(36) NOT NULL,
    estado VARCHAR(50) NOT NULL,
    monto_apertura DECIMAL(19, 4) NOT NULL,
    CONSTRAINT pk_pos_turno_caja PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices Multitenant (MT-01) y de Rendimiento
CREATE INDEX idx_pos_turno_caja_empresa ON pos_turno_caja (empresa_id);
CREATE INDEX idx_pos_turno_caja_empresa_caja ON pos_turno_caja (empresa_id, caja_id);
CREATE INDEX idx_pos_turno_caja_empresa_estado ON pos_turno_caja (empresa_id, estado);

-- 2. Tabla: pos_transaccion_caja (Entidad Local)
CREATE TABLE IF NOT EXISTS pos_transaccion_caja (
    id VARCHAR(36) NOT NULL,
    turno_id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    monto DECIMAL(19, 4) NOT NULL,
    referencia VARCHAR(255),
    fecha DATETIME NOT NULL,
    CONSTRAINT pk_pos_transaccion_caja PRIMARY KEY (id),
    CONSTRAINT fk_pos_transaccion_caja_turno FOREIGN KEY (turno_id)
        REFERENCES pos_turno_caja (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices Multitenant (MT-01)
CREATE INDEX idx_pos_transaccion_caja_empresa ON pos_transaccion_caja (empresa_id);
CREATE INDEX idx_pos_transaccion_caja_turno ON pos_transaccion_caja (turno_id);
