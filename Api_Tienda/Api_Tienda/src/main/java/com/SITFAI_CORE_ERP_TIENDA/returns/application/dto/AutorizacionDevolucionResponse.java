package com.SITFAI_CORE_ERP_TIENDA.returns.application.dto;

import java.util.List;
import java.util.UUID;

public record AutorizacionDevolucionResponse(
        UUID devolucionId,
        UUID empresaId,
        UUID documentoFuenteId,
        String estado,
        List<LineaDevolucionResponse> lineas
) {}
