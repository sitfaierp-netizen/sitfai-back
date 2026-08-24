-- Corrección para schema-validation: agregar actualizado_en a sourcing_proveedores
ALTER TABLE sourcing_proveedores
    ADD COLUMN actualizado_en TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
