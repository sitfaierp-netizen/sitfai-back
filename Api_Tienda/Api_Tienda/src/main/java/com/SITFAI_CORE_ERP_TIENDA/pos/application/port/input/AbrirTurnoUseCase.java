package com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.AbrirTurnoCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;

public interface AbrirTurnoUseCase {
    TurnoCajaResponse ejecutar(AbrirTurnoCommand command);
}
