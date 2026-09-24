-- ============================================================
-- V50__init_pedido_schema.sql
-- Módulo: Api_Tienda (Comercio / Ventas / Pedidos)
-- Descripción: Tablas para el ciclo de vida del Agregado Pedido y Líneas de Pedido.
-- Regla MT-01: empresa_id como clave de partición multi-tenant OBLIGATORIA.
-- Regla MONEY-01: total y precios unitarios persistidos con DECIMAL(19,4).
-- Regla AUD-01: Campos de auditoría created_at/by y updated_at/by (AuditableJpaEntity).
-- Regla CON-01: Concurrencia optimista con columna version (@Version).
-- ============================================================

-- 1. Tabla: tienda_pedido (Aggregate Root Pedido)
CREATE TABLE IF NOT EXISTS tienda_pedido (
    id              VARCHAR(36)    NOT NULL COMMENT 'UUID del Pedido (PK)',
    empresa_id      VARCHAR(36)    NOT NULL COMMENT 'Tenant discriminator — MT-01',
    cliente_id      VARCHAR(36)    NOT NULL COMMENT 'Identificador del cliente',
    estado          VARCHAR(30)    NOT NULL COMMENT 'Estado del pedido (PENDIENTE, CONFIRMADO, CANCELADO)',
    total           DECIMAL(19, 4) NOT NULL DEFAULT 0.0000 COMMENT 'Importe monetario total (MONEY-01)',
    moneda          VARCHAR(3)     NOT NULL DEFAULT 'USD' COMMENT 'Código de moneda ISO 4217',
    version         INT            NOT NULL DEFAULT 0 COMMENT 'Control de concurrencia optimista (CON-01)',
    creado_en       TIMESTAMP(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    creado_por      VARCHAR(255)   NOT NULL DEFAULT 'SYSTEM',
    actualizado_en  TIMESTAMP(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    actualizado_por VARCHAR(255)   NULL,

    CONSTRAINT pk_tienda_pedido PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Pedidos comerciales de venta (Api_Tienda Aggregate Root)';

-- 2. Tabla: tienda_linea_pedido (Líneas de detalle del Pedido)
CREATE TABLE IF NOT EXISTS tienda_linea_pedido (
    id              VARCHAR(36)    NOT NULL COMMENT 'UUID de la Línea de Pedido (PK)',
    pedido_id       VARCHAR(36)    NOT NULL COMMENT 'FK hacia tienda_pedido',
    empresa_id      VARCHAR(36)    NOT NULL COMMENT 'Tenant discriminator — MT-01',
    producto_id     VARCHAR(36)    NOT NULL COMMENT 'Producto SKU o UUID',
    cantidad        INT            NOT NULL COMMENT 'Cantidad solicitada',
    precio_unitario DECIMAL(19, 4) NOT NULL COMMENT 'Precio unitario acordado (MONEY-01)',
    moneda          VARCHAR(3)     NOT NULL DEFAULT 'USD' COMMENT 'Código de moneda',
    subtotal        DECIMAL(19, 4) NOT NULL COMMENT 'Subtotal calculado de la línea',
    version         INT            NOT NULL DEFAULT 0 COMMENT 'Control de concurrencia optimista (CON-01)',
    creado_en       TIMESTAMP(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    creado_por      VARCHAR(255)   NOT NULL DEFAULT 'SYSTEM',
    actualizado_en  TIMESTAMP(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    actualizado_por VARCHAR(255)   NULL,

    CONSTRAINT pk_tienda_linea_pedido PRIMARY KEY (id),
    CONSTRAINT fk_tienda_linea_pedido FOREIGN KEY (pedido_id)
        REFERENCES tienda_pedido (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='Líneas de detalle de los pedidos comerciales';

-- Índices compuestos para alto rendimiento y particionamiento estricto MT-01
CREATE INDEX idx_tienda_pedido_empresa_cliente_estado 
    ON tienda_pedido (empresa_id, cliente_id, estado);

CREATE INDEX idx_tienda_linea_pedido_empresa_pedido 
    ON tienda_linea_pedido (empresa_id, pedido_id);
