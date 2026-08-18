package com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.CrearCajaCommand;
import java.util.UUID;

public interface CrearCajaUseCase {
    UUID ejecutar(CrearCajaCommand command);
}
