package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ReingresarStockPorDevolucionCommand;

public interface ReingresarStockPorDevolucionUseCase {
    void ejecutar(ReingresarStockPorDevolucionCommand command);
}
