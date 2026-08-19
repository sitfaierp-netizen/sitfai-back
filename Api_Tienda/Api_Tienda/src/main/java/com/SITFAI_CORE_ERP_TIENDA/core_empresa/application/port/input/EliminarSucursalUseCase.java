package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.input;

import java.util.UUID;

public interface EliminarSucursalUseCase {
    void ejecutar(UUID empresaId, UUID sucursalId);
}
