package com.SITFAI_CORE_ERP_TIENDA.returns.application.dto;

import java.util.UUID;

public record InspeccionarDevolucionCommand(
        UUID devolucionId,
        UUID productoId,
        boolean aprobado
) {}
