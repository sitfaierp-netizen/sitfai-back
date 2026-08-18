package com.SITFAI_CORE_ERP_TIENDA.catalog.domain.exception;

/** Producto no encontrado en el catálogo del tenant. */
public class ProductoNoEncontradoException extends DomainException {
    public ProductoNoEncontradoException(String productoId) {
        super("Producto no encontrado en el catálogo: " + productoId);
    }
}
