package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto;

import java.util.UUID;

/**
 * Web Response DTO: Detalle de línea en la respuesta HTTP de un conteo cíclico.
 */
public record DetalleConteoWebResponse(
        UUID lineaId,
        UUID productoId,
        int cantidadTeorica,
        Integer cantidadFisica,
        int diferencia,
        boolean tieneDiscrepancia
) {
}
