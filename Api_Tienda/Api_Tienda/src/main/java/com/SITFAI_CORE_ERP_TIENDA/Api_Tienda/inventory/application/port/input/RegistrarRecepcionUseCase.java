package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarRecepcionCommand;

import java.util.UUID;

public interface RegistrarRecepcionUseCase {
    UUID registrar(RegistrarRecepcionCommand command);
}
