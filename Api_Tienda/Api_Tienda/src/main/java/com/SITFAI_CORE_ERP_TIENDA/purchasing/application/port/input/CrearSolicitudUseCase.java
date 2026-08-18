package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.CrearSolicitudCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.SolicitudResponse;

/**
 * Driving Port (Input): Caso de uso para crear una SolicitudAbastecimiento.
 * Implementado por CrearSolicitudService en la capa de aplicación.
 */
public interface CrearSolicitudUseCase {

    /**
     * Crea un borrador de solicitud de abastecimiento y lo persiste.
     *
     * @param command Comando con los datos de la solicitud y sus líneas.
     * @return        DTO de respuesta con el ID y estado de la solicitud creada.
     */
    SolicitudResponse crear(CrearSolicitudCommand command);
}
