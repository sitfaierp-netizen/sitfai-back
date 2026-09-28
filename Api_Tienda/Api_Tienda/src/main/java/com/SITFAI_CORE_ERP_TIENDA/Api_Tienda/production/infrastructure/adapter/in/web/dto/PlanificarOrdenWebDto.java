package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.in.web.dto;

import java.util.UUID;

public record PlanificarOrdenWebDto(UUID recetaId, UUID bodegaId, Integer cantidadProducir) {
}
