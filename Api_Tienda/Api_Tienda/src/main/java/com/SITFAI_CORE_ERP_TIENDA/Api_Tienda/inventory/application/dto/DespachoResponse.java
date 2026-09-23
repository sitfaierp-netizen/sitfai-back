package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO de Respuesta de Aplicación tras confirmar el Despacho de mercancía.
 */
public record DespachoResponse(
        UUID despachoId,
        UUID pedidoId,
        UUID bodegaId,
        String estado,
        int totalLineas,
        Instant fechaDespacho,
        String mensaje
) {
}
