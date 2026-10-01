package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ConfigurarPuntoReordenCommand;

/** Driving port for configuring the reorder point of a warehouse product. */
public interface ConfigurarPuntoReordenUseCase {
    void ejecutar(ConfigurarPuntoReordenCommand command);
}
