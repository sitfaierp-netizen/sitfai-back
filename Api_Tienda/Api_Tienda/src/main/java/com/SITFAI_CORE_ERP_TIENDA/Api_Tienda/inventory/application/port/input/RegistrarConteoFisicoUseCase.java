package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ConteoCiclicoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarConteoFisicoCommand;

/**
 * Driving Input Port: Caso de Uso para registrar la cantidad física constatada en un conteo cíclico.
 * <p>
 * Regla REGLA-1: Interfaz pura de aplicación.
 */
public interface RegistrarConteoFisicoUseCase {

    ConteoCiclicoResponse registrar(RegistrarConteoFisicoCommand command);
}
