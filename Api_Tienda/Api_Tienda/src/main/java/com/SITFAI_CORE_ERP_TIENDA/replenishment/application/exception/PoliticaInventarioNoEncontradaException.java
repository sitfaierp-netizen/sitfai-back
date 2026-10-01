package com.SITFAI_CORE_ERP_TIENDA.replenishment.application.exception;

import java.util.UUID;

public class PoliticaInventarioNoEncontradaException extends RuntimeException {
    public PoliticaInventarioNoEncontradaException(UUID id) {
        super("Política de inventario no encontrada: " + id);
    }
}
