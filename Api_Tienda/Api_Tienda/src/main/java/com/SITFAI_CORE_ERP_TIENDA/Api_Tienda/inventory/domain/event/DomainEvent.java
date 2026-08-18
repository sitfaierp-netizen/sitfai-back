package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Interfaz sellada (sealed) — marca raíz de todos los Eventos de Dominio
 * del Bounded Context de Inventario.
 * <p>
 * Los Domain Events son inmutables (records), contienen timestamp, el ID del
 * Agregado que los generó y los datos mínimos necesarios para ser procesados
 * por otros Bounded Contexts (REGLA-3).
 * <p>
 * Los consumidores (Spring Events, Kafka producers) viven en Infraestructura.
 * El Dominio solo EMITE — nunca consume ni publica directamente (MCP-05).
 * <p>
 * Reglas validadas: REGLA-3 (Domain Events), AUD-03 (Event Store), MCP-01.
 */
public sealed interface DomainEvent
        permits MovimientoRegistradoEvent, StockActualizadoEvent, PuntoReordenAlcanzadoEvent {

    /**
     * Identificador único del evento — permite idempotencia en consumidores.
     */
    UUID eventoId();

    /**
     * Marca de tiempo en que ocurrió el evento en el dominio.
     */
    Instant ocurridoEn();
}
