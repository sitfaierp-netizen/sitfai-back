package com.SITFAI_CORE_ERP_TIENDA.returns.infrastructure.adapter.in.web.dto;

import java.util.UUID;

public record InspeccionarRmaWebRequest(
        UUID productoId,
        boolean aprobado
) {}
