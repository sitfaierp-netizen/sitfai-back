package com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto;

/**
 * Response DTO con el balance métrico de la ejecución del lote de eventos Outbox.
 */
public record ProcesarEventosResponse(
        int procesados,
        int fallidos,
        int total
) {
    public static ProcesarEventosResponse vacio() {
        return new ProcesarEventosResponse(0, 0, 0);
    }
}
