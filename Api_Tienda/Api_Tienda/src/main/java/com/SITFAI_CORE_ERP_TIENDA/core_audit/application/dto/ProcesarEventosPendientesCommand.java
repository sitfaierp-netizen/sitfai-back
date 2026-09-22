package com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto;

/**
 * Command DTO inmutable para la ejecución del lote de procesamiento de eventos pendientes en el Outbox.
 */
public record ProcesarEventosPendientesCommand(int batchSize) {

    public ProcesarEventosPendientesCommand {
        if (batchSize <= 0) {
            batchSize = 50;
        }
    }

    public static ProcesarEventosPendientesCommand porDefecto() {
        return new ProcesarEventosPendientesCommand(50);
    }
}
