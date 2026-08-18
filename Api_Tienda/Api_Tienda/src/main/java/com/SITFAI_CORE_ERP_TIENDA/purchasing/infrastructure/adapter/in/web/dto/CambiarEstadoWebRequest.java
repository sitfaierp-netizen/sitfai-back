package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web.dto;

/**
 * DTO Web: Petición para modificar el estado de la orden (APROBAR, RECIBIR, CANCELAR).
 */
public record CambiarEstadoWebRequest(
        String estado,
        String motivo
) {
}
