package com.SITFAI_CORE_ERP_TIENDA.returns.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.returns.application.dto.AutorizacionDevolucionResponse;
import com.SITFAI_CORE_ERP_TIENDA.returns.application.dto.CrearAutorizacionDevolucionCommand;
import com.SITFAI_CORE_ERP_TIENDA.returns.application.dto.InspeccionarDevolucionCommand;

public interface GestionarDevolucionUseCase {
    AutorizacionDevolucionResponse crearAutorizacion(CrearAutorizacionDevolucionCommand command);
    AutorizacionDevolucionResponse inspeccionar(InspeccionarDevolucionCommand command);
}
