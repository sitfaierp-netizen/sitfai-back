package com.SITFAI_CORE_ERP_TIENDA.returns.infrastructure.adapter.in.web.dto;

import java.util.UUID;

public record LineaRmaWebRequest(
        UUID productoId,
        int cantidad,
        String motivo
) {}
