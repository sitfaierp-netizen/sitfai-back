-- SITFAI ERP — Migración V9: Mejora de Índices de Api_Tienda (empresa_id como prefijo en índices de fecha y producto)
-- Stack: MySQL 8.0+ / InnoDB / utf8mb4
-- Reglas aplicadas: MT-01 (Multitenancy empresa_id en todos los índices de consulta)
-- NOTA: V2 ya creó las tablas tienda_pedido y tienda_linea_pedido.
--       Esta migración únicamente mejora los índices de rendimiento para incluir empresa_id como prefijo.

-- Mejorar idx_tienda_pedido_fecha: añadir empresa_id como prefijo para garantizar aislamiento multitenant
DROP INDEX idx_tienda_pedido_fecha ON tienda_pedido;
CREATE INDEX idx_tienda_pedido_fecha ON tienda_pedido (empresa_id, creado_en);

-- Mejorar idx_tienda_linea_pedido_producto: añadir empresa_id como prefijo para garantizar aislamiento multitenant
DROP INDEX idx_tienda_linea_pedido_producto ON tienda_linea_pedido;
CREATE INDEX idx_tienda_linea_pedido_producto ON tienda_linea_pedido (empresa_id, producto_id);
