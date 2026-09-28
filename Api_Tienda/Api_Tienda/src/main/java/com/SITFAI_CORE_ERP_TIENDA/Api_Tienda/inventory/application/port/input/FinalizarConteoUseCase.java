package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ConteoCiclicoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.FinalizarConteoCommand;

/**
 * Driving Input Port: Caso de Uso para finalizar la auditoría de un conteo cíclico y emitir eventos de discrepancias.
 * <p>
 * Regla REGLA-1: Interfaz pura de aplicación.
 */
public interface FinalizarConteoUseCase {

    ConteoCiclicoResponse finalizar(FinalizarConteoCommand command);
}
