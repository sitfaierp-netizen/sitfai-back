package com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.AnularFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;

/**
 * Driving Port (Puerto de Entrada): Caso de uso para anular una Factura emitida.
 */
public interface AnularFacturaUseCase {

    FacturaResponse ejecutar(AnularFacturaCommand command);
}
