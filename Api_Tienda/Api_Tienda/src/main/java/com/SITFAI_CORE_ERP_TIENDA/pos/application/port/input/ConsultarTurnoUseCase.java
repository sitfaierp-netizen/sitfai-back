package com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;

import java.util.UUID;

public interface ConsultarTurnoUseCase {
    TurnoCajaResponse porId(UUID id, UUID empresaId);
}
