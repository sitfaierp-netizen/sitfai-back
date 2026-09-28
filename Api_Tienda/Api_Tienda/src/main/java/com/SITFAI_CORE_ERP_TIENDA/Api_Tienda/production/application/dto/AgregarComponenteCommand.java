package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AgregarComponenteCommand(UUID recetaId, UUID insumoId, BigDecimal cantidad) {
}
