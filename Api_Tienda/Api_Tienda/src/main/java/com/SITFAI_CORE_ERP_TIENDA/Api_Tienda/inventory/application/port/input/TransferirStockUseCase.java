package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.TransferirStockCommand;

/** Driving port for stock transfers between warehouses. */
public interface TransferirStockUseCase {
    void ejecutar(TransferirStockCommand command);
}
