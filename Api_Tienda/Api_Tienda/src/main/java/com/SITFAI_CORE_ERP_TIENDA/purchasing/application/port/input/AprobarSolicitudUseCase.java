package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.AprobarSolicitudCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.SolicitudResponse;

/**
 * Driving Port (Input): Caso de uso para aprobar una SolicitudAbastecimiento.
 * Implementado por AprobarSolicitudService en la capa de aplicación.
 */
public interface AprobarSolicitudUseCase {

    /**
     * Aprueba una solicitud en estado PENDIENTE_APROBACION.
     *
     * @param command Comando con el SolicitudId y el EmpresaId del aprobador (MT-01).
     * @return        DTO con el estado actualizado de la solicitud.
     */
    SolicitudResponse aprobar(AprobarSolicitudCommand command);
}
