-- SITFAI ERP — Migración V39: Adaptación de esquema para agregados de E-Commerce
-- Stack: MySQL 8.0+ / InnoDB / utf8mb4
-- Reglas aplicadas: AUD-01 (Auditoría de persistencia), Optimistic Locking (@Version)

-- Agregar campos de auditoría y versión a tienda_pedido
ALTER TABLE tienda_pedido
    ADD COLUMN creado_por VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN actualizado_por VARCHAR(255),
    ADD COLUMN version INT NOT NULL DEFAULT 0;

-- Agregar campos de auditoría, fechas y versión a tienda_linea_pedido
ALTER TABLE tienda_linea_pedido
    ADD COLUMN creado_en TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN creado_por VARCHAR(255) NOT NULL DEFAULT 'SYSTEM',
    ADD COLUMN actualizado_en TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    ADD COLUMN actualizado_por VARCHAR(255),
    ADD COLUMN version INT NOT NULL DEFAULT 0;
