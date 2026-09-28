package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto;

import java.util.UUID;

public record PlanificarOrdenCommand(UUID recetaId, UUID bodegaId, Integer cantidadProducir) {
}
