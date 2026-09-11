ALTER TABLE orders_pedido
    RENAME COLUMN created_at TO creado_en,
    RENAME COLUMN created_by TO creado_por,
    RENAME COLUMN updated_at TO actualizado_en,
    RENAME COLUMN updated_by TO actualizado_por;
