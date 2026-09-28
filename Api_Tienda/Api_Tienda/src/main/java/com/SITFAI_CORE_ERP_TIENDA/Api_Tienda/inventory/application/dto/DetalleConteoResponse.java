package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.util.UUID;

/**
 * DTO inmutable de salida: Detalle individual de una línea auditada en el conteo cíclico.
 */
public record DetalleConteoResponse(
        UUID lineaId,
        UUID productoId,
        int cantidadTeorica,
        Integer cantidadFisica,
        int diferencia,
        boolean tieneDiscrepancia
) {
}
