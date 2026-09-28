package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.CompletarRecepcionCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarProductoRecibidoCommand;

public interface GestionarRecepcionUseCase {
    void registrarProductoRecibido(RegistrarProductoRecibidoCommand command);
    void completarRecepcion(CompletarRecepcionCommand command);
}
