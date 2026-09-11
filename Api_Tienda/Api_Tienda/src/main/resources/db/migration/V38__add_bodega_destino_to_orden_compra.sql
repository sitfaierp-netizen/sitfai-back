-- V38: Agrega la columna bodega_destino_id a la tabla de órdenes de compra.
-- Esta columna registra la bodega de destino donde se recibirá el stock
-- al momento de confirmar la recepción (estado RECIBIDO).
-- Nullable para compatibilidad con registros previos a este cambio.
ALTER TABLE purchasing_orden_compra
    ADD COLUMN bodega_destino_id VARCHAR(36) NULL;
