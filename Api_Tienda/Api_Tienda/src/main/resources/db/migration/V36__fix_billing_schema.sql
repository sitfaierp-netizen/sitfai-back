DROP TABLE IF EXISTS billing_linea_factura;
DROP TABLE IF EXISTS billing_factura;

CREATE TABLE billing_factura (
    id BINARY(16) NOT NULL,
    empresa_id BINARY(16) NOT NULL,
    cliente_id BINARY(16) NOT NULL,
    pedido_id BINARY(16),
    ruc_cliente VARCHAR(13) NOT NULL,
    subtotal DECIMAL(19,4) NOT NULL,
    total_impuestos DECIMAL(19,4) NOT NULL,
    total_general DECIMAL(19,4) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    creado_en TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    creado_por VARCHAR(100) NOT NULL DEFAULT 'system',
    actualizado_en TIMESTAMP,
    actualizado_por VARCHAR(100),
    CONSTRAINT pk_billing_factura PRIMARY KEY (id),
    CONSTRAINT uq_billing_factura_empresa_id_uuid UNIQUE (empresa_id, id)
);

CREATE TABLE billing_linea_factura (
    id BINARY(16) NOT NULL,
    factura_id BINARY(16) NOT NULL,
    concepto VARCHAR(255) NOT NULL,
    cantidad DECIMAL(19,4) NOT NULL,
    precio_unitario DECIMAL(19,4) NOT NULL,
    subtotal DECIMAL(19,4) NOT NULL,
    total_impuestos DECIMAL(19,4) NOT NULL,
    CONSTRAINT pk_billing_linea_factura PRIMARY KEY (id),
    CONSTRAINT fk_billing_linea_factura_factura FOREIGN KEY (factura_id) REFERENCES billing_factura (id) ON DELETE CASCADE
);
