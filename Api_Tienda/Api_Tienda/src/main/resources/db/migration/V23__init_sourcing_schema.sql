-- =============================================================================
-- SITFAI ERP — Flyway V23
-- Módulo: sourcing | Agregados: Proveedor
-- Reglas: MT-01, UNIQUE constraints multitenant
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Tabla: sourcing_proveedores
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sourcing_proveedores (
    id             VARCHAR(36)    NOT NULL,
    empresa_id     VARCHAR(36)    NOT NULL COMMENT 'Discriminador de tenant (MT-01)',
    ruc            VARCHAR(50)    NOT NULL,
    razon_social   VARCHAR(255)   NOT NULL,
    email_contacto VARCHAR(100)   NULL,
    telefono       VARCHAR(50)    NULL,
    direccion      VARCHAR(500)   NULL,
    estado         VARCHAR(20)    NOT NULL,
    creado_en      DATETIME(6)    NOT NULL,

    CONSTRAINT pk_sourcing_proveedores PRIMARY KEY (id),
    CONSTRAINT uq_sourcing_prov_empresa_ruc UNIQUE (empresa_id, ruc)
);

CREATE INDEX idx_sourcing_prov_empresa ON sourcing_proveedores(empresa_id);
CREATE INDEX idx_sourcing_prov_empresa_estado ON sourcing_proveedores(empresa_id, estado);
