package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AgregarComponenteWebDto(UUID insumoId, BigDecimal cantidad) {
}
