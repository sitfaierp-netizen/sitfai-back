-- =============================================================================
-- SITFAI ERP — Flyway V21
-- Módulo: purchasing | Agregado: SolicitudAbastecimiento
-- Reglas: MT-01 (empresa_id obligatorio), UUID como PK, snake_case, DECIMAL(19,4)
-- ADR-008: ddl-auto=validate — esquema gestionado exclusivamente por Flyway
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Tabla principal: purchasing_solicitud
-- Representa el Aggregate Root SolicitudAbastecimiento
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS purchasing_solicitud (
    id         VARCHAR(36)  NOT NULL,
    empresa_id VARCHAR(36)  NOT NULL COMMENT 'Discriminador de tenant (MT-01)',
    bodega_id  VARCHAR(36)  NOT NULL COMMENT 'Bodega destino que solicita el reabastecimiento',
    estado     VARCHAR(30)  NOT NULL COMMENT 'BORRADOR | PENDIENTE_APROBACION | APROBADA | RECHAZADA | PROCESADA',
    creado_en  DATETIME(6)  NOT NULL,

    CONSTRAINT pk_purchasing_solicitud PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices de rendimiento y aislamiento multitenant (MT-01)
CREATE INDEX idx_purchasing_sol_empresa
    ON purchasing_solicitud (empresa_id);

CREATE INDEX idx_purchasing_sol_empresa_estado
    ON purchasing_solicitud (empresa_id, estado);

CREATE INDEX idx_purchasing_sol_empresa_bodega
    ON purchasing_solicitud (empresa_id, bodega_id);

-- -----------------------------------------------------------------------------
-- Tabla de líneas: purchasing_linea_solicitud
-- Entidad LineaSolicitud — hija del Agregado SolicitudAbastecimiento
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS purchasing_linea_solicitud (
    id                  VARCHAR(36)    NOT NULL,
    solicitud_id        VARCHAR(36)    NOT NULL COMMENT 'FK al Agregado raíz',
    empresa_id          VARCHAR(36)    NOT NULL COMMENT 'Discriminador de tenant (MT-01)',
    producto_id         VARCHAR(36)    NOT NULL,
    cantidad_solicitada DECIMAL(19, 4) NOT NULL COMMENT 'Cantidad > 0, precisión contable HALF_UP',

    CONSTRAINT pk_purchasing_linea_solicitud PRIMARY KEY (id),
    CONSTRAINT fk_purchasing_linea_solicitud_solicitud
        FOREIGN KEY (solicitud_id) REFERENCES purchasing_solicitud (id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices de rendimiento en líneas
CREATE INDEX idx_purchasing_lsol_solicitud
    ON purchasing_linea_solicitud (solicitud_id);

CREATE INDEX idx_purchasing_lsol_empresa
    ON purchasing_linea_solicitud (empresa_id);

CREATE INDEX idx_purchasing_lsol_empresa_producto
    ON purchasing_linea_solicitud (empresa_id, producto_id);
