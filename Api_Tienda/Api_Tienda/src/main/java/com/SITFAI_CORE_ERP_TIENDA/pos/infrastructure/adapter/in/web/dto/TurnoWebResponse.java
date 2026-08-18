package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Web Response DTO: Representación JSON del recurso REST Turno de Caja.
 */
public record TurnoWebResponse(
        UUID id,
        UUID cajaId,
        UUID empresaId,
        String estado,
        BigDecimal montoInicial,
        String moneda,
        List<TransaccionWebResponse> transacciones,
        ArqueoWebResponse arqueo,
        Instant abiertoEn,
        Instant cerradoEn,
        Instant creadoEn,
        Instant actualizadoEn
) {}
