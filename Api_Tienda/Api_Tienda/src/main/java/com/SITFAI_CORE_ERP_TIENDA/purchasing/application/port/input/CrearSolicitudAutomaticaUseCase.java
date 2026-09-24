package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearSolicitudAutomaticaCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.SolicitudResponse;

/**
 * Puerto de Entrada (Driving Port): Caso de uso para la creación automática de una
 * Solicitud de Abastecimiento derivada de una alerta del módulo {@code replenishment}.
 * <p>
 * Regla REGLA-1: Interfaz pura sin dependencias de frameworks.
 * Regla Protocolo-5: Comunicación inter-módulos a través de Domain Events (MCP § 5).
 */
public interface CrearSolicitudAutomaticaUseCase {

    /**
     * Crea una Solicitud de Abastecimiento en estado {@code BORRADOR} de forma automática,
     * basándose en la detección de un déficit de stock por el motor de reposición.
     * <p>
     * La solicitud queda en {@code BORRADOR} para que el área de Compras la revise,
     * enriquezca (proveedor, costos) y la envíe a aprobación de forma explícita.
     *
     * @param command Comando con tenant, bodega, producto y cantidad requerida.
     * @return        DTO de respuesta con el ID y estado de la solicitud generada.
     */
    SolicitudResponse crearAutomatica(CrearSolicitudAutomaticaCommand command);
}
