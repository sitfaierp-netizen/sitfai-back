package com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.RegistrarTransaccionCajaCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;

public interface RegistrarTransaccionCajaUseCase {
    TurnoCajaResponse registrarTransaccion(RegistrarTransaccionCajaCommand command);
}
