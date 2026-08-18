package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.AgregarLineaCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;

public interface GestionarLineasUseCase {
    OrdenCompraResponse agregarLinea(AgregarLineaCommand command);
}
