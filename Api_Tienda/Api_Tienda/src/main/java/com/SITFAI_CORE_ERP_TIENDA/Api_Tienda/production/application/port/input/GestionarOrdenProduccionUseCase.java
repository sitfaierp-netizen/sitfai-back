package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.CompletarProduccionCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.IniciarProduccionCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto.PlanificarOrdenCommand;

import java.util.UUID;

public interface GestionarOrdenProduccionUseCase {
    UUID planificarOrden(PlanificarOrdenCommand command);
    void iniciarProduccion(IniciarProduccionCommand command);
    void completarProduccion(CompletarProduccionCommand command);
}
