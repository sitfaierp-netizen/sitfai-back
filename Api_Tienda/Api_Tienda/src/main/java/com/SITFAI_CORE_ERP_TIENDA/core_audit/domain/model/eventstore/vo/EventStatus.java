package com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo;

/**
 * Enumeración de estados de ciclo de vida para un evento almacenado en el Event Store (AUD-03).
 * <p>
 * Diseñado para soportar el patrón transaccional Outbox y trazabilidad distribuida:
 * <ul>
 *   <li><b>PENDIENTE:</b> Evento notariado en la base de datos, en espera de despacho asíncrono o consumo.</li>
 *   <li><b>PROCESADO:</b> Evento efectivamente despachado o consumido por los suscriptores.</li>
 *   <li><b>FALLIDO:</b> Evento cuyo procesamiento o entrega por un consumidor reportó un fallo irrecoverable o transitorio.</li>
 * </ul>
 */
public enum EventStatus {
    PENDIENTE,
    PROCESADO,
    FALLIDO
}
