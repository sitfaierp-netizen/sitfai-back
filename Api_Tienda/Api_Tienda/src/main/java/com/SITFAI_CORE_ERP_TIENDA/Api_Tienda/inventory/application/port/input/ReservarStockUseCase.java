package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ReservarStockCommand;

public interface ReservarStockUseCase {
    void ejecutar(ReservarStockCommand command);
}
