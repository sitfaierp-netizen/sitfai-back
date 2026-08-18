package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.util.UUID;

public record AprobarOrdenCommand(
        UUID empresaId,
        UUID ordenCompraId
) {
}
