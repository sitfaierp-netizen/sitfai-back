package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.OrdenDespachoResponse;
import java.util.UUID;

public interface ConsultarDespachoUseCase {
    OrdenDespachoResponse porId(UUID id, UUID empresaId);
}
