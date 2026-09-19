package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.query.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO plano de lectura (CQRS) para representar el historial de movimientos de Kárdex.
 */
public record MovimientoKardexView(
    String id,
    String empresaId,
    String bodegaId,
    String productoId,
    String tipo,
    BigDecimal cantidad,
    String docFuenteTipo,
    String docFuenteNumero,
    Instant fechaRegistro
) {}
