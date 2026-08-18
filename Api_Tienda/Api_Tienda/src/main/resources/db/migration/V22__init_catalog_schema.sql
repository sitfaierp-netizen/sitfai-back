-- =============================================================================
-- SITFAI ERP — Flyway V22
-- Módulo: catalog | Agregados: Categoria, Producto
-- Reglas: MT-01, UNIQUE constraints multitenant, DECIMAL(19,4) precision
-- =============================================================================

-- -----------------------------------------------------------------------------
-- Tabla: catalog_categorias
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS catalog_categorias (
    id                 VARCHAR(36)  NOT NULL,
    empresa_id         VARCHAR(36)  NOT NULL COMMENT 'Discriminador de tenant (MT-01)',
    nombre             VARCHAR(255) NOT NULL,
    categoria_padre_id VARCHAR(36)  NULL     COMMENT 'Nulo si es categoría raíz',
    estado             VARCHAR(20)  NOT NULL,
    creado_en          DATETIME(6)  NOT NULL,

    CONSTRAINT pk_catalog_categorias PRIMARY KEY (id),
    CONSTRAINT uq_catalog_cat_empresa_nombre UNIQUE (empresa_id, nombre)
);

CREATE INDEX idx_catalog_cat_empresa ON catalog_categorias(empresa_id);

-- -----------------------------------------------------------------------------
-- Tabla: catalog_productos
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS catalog_productos (
    id             VARCHAR(36)    NOT NULL,
    empresa_id     VARCHAR(36)    NOT NULL COMMENT 'Discriminador de tenant (MT-01)',
    sku            VARCHAR(100)   NOT NULL,
    nombre         VARCHAR(255)   NOT NULL,
    descripcion    VARCHAR(2000)  NULL,
    categoria_id   VARCHAR(36)    NOT NULL,
    unidad_medida  VARCHAR(20)    NOT NULL,
    precio_compra  DECIMAL(19, 4) NOT NULL,
    precio_venta   DECIMAL(19, 4) NOT NULL,
    impuesto       VARCHAR(20)    NOT NULL,
    codigo_barras  VARCHAR(50)    NULL,
    estado         VARCHAR(20)    NOT NULL,
    creado_en      DATETIME(6)    NOT NULL,
    actualizado_en DATETIME(6)    NOT NULL,

    CONSTRAINT pk_catalog_productos PRIMARY KEY (id),
    CONSTRAINT uq_catalog_prod_empresa_sku UNIQUE (empresa_id, sku),
    CONSTRAINT fk_catalog_productos_categoria FOREIGN KEY (categoria_id) REFERENCES catalog_categorias(id),
    CONSTRAINT chk_catalog_prod_precio_venta CHECK (precio_venta > 0),
    CONSTRAINT chk_catalog_prod_precio_compra CHECK (precio_compra >= 0)
);

CREATE INDEX idx_catalog_prod_empresa ON catalog_productos(empresa_id);
CREATE INDEX idx_catalog_prod_empresa_estado ON catalog_productos(empresa_id, estado);
CREATE INDEX idx_catalog_prod_categoria ON catalog_productos(categoria_id);
