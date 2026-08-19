package com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.CrearProductoCommand;
import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.ProductoResponse;

/**
 * Driving Port (Input): Caso de uso para crear un Producto en el Catálogo.
 * Implementado por CrearProductoService.
 */
public interface CrearProductoUseCase {
    ProductoResponse crear(CrearProductoCommand command);
}
