package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.IncrementarStockCommand;

public interface IncrementarStockUseCase {
    void ejecutar(IncrementarStockCommand command);
}
