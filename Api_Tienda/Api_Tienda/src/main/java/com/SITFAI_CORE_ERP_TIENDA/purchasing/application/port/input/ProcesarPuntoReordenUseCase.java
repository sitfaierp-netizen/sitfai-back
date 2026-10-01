package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.ProcesarPuntoReordenCommand;

/** Inbound port for the trusted Inventory point-of-reorder event path. */
public interface ProcesarPuntoReordenUseCase {

    OrdenCompraResponse procesar(ProcesarPuntoReordenCommand command);
}
