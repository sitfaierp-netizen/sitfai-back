package com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.ProcesarDevolucionCommand;

public interface ProcesarDevolucionUseCase {
    void ejecutar(ProcesarDevolucionCommand command);
}
