package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.util.UUID;

public record ProveedorResponse(
        UUID id,
        String ruc,
        String razonSocial
) {}
