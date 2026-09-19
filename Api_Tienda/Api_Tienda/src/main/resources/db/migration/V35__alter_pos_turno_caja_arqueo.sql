-- Migración V35: Cierre de Turno y Arqueo de Caja (CAJ-04, CAJ-05) y Control de Concurrencia (CON-01)

-- 1. Añadir columnas de arqueo a la tabla pos_turno_caja
ALTER TABLE pos_turno_caja ADD COLUMN monto_esperado DECIMAL(19,4);
ALTER TABLE pos_turno_caja ADD COLUMN monto_declarado DECIMAL(19,4);
ALTER TABLE pos_turno_caja ADD COLUMN diferencia DECIMAL(19,4);

-- 2. Añadir columna version para Concurrencia Optimista
ALTER TABLE pos_turno_caja ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
