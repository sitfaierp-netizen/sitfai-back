package com.SITFAI_CORE_ERP_TIENDA.core_audit.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.ProcesarEventosPendientesCommand;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.application.dto.ProcesarEventosResponse;

/**
 * Driving Port / Caso de Uso: Procesamiento y Despacho de Eventos Pendientes (Outbox Dispatcher).
 */
public interface ProcesarEventosPendientesUseCase {

    /**
     * Recupera y procesa un lote de eventos pendientes en el Event Store.
     *
     * @param command Parámetros de ejecución (tamaño de lote).
     * @return Balance de eventos procesados y fallidos.
     */
    ProcesarEventosResponse procesarPendientes(ProcesarEventosPendientesCommand command);

    /**
     * Sobrecarga conveniente que procesa un lote con tamaño por defecto.
     */
    default ProcesarEventosResponse procesarPendientes() {
        return procesarPendientes(ProcesarEventosPendientesCommand.porDefecto());
    }
}
