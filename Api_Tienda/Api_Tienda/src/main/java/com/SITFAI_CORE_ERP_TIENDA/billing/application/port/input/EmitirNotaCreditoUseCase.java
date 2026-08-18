package com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirNotaCreditoCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.NotaCreditoResponse;

/**
 * Driving Port: Caso de Uso para Emitir una Nota de Crédito.
 */
public interface EmitirNotaCreditoUseCase {
    NotaCreditoResponse emitirNotaCredito(EmitirNotaCreditoCommand command);
}
