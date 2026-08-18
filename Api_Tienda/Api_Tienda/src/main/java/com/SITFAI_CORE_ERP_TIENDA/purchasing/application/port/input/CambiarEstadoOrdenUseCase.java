package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CambiarEstadoCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;

public interface CambiarEstadoOrdenUseCase {
    OrdenCompraResponse cambiarEstado(CambiarEstadoCommand command);
}
