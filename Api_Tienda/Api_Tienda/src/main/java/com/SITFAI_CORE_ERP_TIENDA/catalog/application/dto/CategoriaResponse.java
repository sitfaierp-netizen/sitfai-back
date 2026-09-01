package com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto;

import java.time.Instant;

public record CategoriaResponse(
        String id,
        String empresaId,
        String nombre,
        String categoriaPadreId,
        String estado,
        Instant creadoEn
) {}
