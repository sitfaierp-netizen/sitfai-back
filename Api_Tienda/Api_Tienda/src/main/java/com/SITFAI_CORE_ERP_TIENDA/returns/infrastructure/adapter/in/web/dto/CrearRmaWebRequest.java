package com.SITFAI_CORE_ERP_TIENDA.returns.infrastructure.adapter.in.web.dto;

import java.util.List;
import java.util.UUID;

public record CrearRmaWebRequest(
        UUID documentoFuenteId,
        List<LineaRmaWebRequest> lineas
) {}

