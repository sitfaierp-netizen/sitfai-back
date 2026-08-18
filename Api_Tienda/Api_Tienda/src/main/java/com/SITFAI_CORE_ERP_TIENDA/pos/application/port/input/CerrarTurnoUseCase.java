package com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.CerrarTurnoCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;

public interface CerrarTurnoUseCase {
    TurnoCajaResponse ejecutar(CerrarTurnoCommand command);
}
