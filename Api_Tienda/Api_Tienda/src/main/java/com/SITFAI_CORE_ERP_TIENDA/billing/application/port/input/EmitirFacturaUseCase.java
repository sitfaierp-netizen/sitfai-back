package com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;

/**
 * Driving Port: Caso de Uso para Emitir una Factura Electrónica.
 */
public interface EmitirFacturaUseCase {
    FacturaResponse emitirFactura(EmitirFacturaCommand command);
}
