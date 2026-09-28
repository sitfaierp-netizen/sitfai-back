package com.SITFAI_CORE_ERP_TIENDA.returns.application.dto;

import java.util.List;
import java.util.UUID;

public record CrearAutorizacionDevolucionCommand(
        UUID documentoFuenteId,
        List<LineaDevolucionCommand> lineas
) {}
