package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Web Response DTO: Representación JSON de una Factura para la capa de presentación HTTP.
 */
public record FacturaWebResponse(
        UUID id,
        UUID empresaId,
        UUID pedidoOrigenId,
        String rucCliente,
        String estado,
        BigDecimal total,
        String moneda,
        Instant creadoEn,
        Instant actualizadoEn
) {}
