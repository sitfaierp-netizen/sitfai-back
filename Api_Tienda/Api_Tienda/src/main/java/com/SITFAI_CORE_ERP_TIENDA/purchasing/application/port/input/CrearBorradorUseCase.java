package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearBorradorCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;

public interface CrearBorradorUseCase {
    OrdenCompraResponse crearBorrador(CrearBorradorCommand command);
}
