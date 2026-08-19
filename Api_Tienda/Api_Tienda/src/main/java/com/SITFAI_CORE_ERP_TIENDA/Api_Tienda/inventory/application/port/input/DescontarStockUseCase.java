package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.DescontarStockCommand;

public interface DescontarStockUseCase {
    void descontarStock(DescontarStockCommand command);
}
