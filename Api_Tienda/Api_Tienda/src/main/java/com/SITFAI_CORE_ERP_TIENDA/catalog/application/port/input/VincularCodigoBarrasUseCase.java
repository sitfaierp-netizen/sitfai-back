package com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto.ProductoResponse;
import java.util.UUID;

/**
 * Driving Port (Input): Caso de uso para vincular un código de barras a un Producto.
 */
public interface VincularCodigoBarrasUseCase {
    ProductoResponse vincularCodigoBarras(UUID productoId, String codigoBarras);
}
