package com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.input;

import java.util.UUID;

public interface EliminarProductoUseCase {
    void eliminar(UUID productoId);
}
