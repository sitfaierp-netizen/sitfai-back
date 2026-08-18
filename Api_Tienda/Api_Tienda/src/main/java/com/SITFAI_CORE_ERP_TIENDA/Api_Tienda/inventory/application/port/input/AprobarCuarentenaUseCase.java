package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.AprobarCuarentenaCommand;

/**
 * Driving Port: Caso de Uso para la inspección y aprobación de cuarentena.
 */
public interface AprobarCuarentenaUseCase {
    void aprobarCuarentena(AprobarCuarentenaCommand command);
}
