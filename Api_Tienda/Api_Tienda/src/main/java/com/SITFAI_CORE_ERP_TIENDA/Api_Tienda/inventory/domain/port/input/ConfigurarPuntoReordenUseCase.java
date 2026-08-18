package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ConfigurarPuntoReordenCommand;

/**
 * Driving Port para configurar el punto de reorden de un producto en la Bodega.
 */
public interface ConfigurarPuntoReordenUseCase {
    void ejecutar(ConfigurarPuntoReordenCommand command);
}
