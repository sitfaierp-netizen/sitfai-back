package com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.ProductoResponse;

import java.util.UUID;

/**
 * Driving Port (Input): Caso de uso para cambiar el estado de un Producto.
 * Implementado por CambiarEstadoProductoService.
 */
public interface CambiarEstadoProductoUseCase {

    /**
     * @param productoId UUID del producto.
     * @param nuevoEstado String del enum EstadoProducto (ACTIVO, INACTIVO, DESCONTINUADO).
     * @return ProductoResponse con el estado actualizado.
     */
    ProductoResponse cambiarEstado(UUID productoId, String nuevoEstado);
}
