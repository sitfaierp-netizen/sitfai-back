package com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.RegistrarTransaccionCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;

public interface RegistrarTransaccionUseCase {
    TurnoCajaResponse ejecutar(RegistrarTransaccionCommand command);
}
