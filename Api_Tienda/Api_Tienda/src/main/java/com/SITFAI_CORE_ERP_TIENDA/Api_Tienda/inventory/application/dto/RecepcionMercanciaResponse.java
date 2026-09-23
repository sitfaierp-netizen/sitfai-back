package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.util.UUID;

/**
 * DTO de Respuesta de Aplicación tras procesar la recepción física en bodega.
 */
public record RecepcionMercanciaResponse(
        UUID bodegaId,
        UUID ordenCompraId,
        int totalLotesRecepcionados,
        String mensaje
) {
}
