package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto;

import java.util.UUID;

public record CompletarPackingCommand(
        UUID empresaId,
        UUID despachoId
) {
}
