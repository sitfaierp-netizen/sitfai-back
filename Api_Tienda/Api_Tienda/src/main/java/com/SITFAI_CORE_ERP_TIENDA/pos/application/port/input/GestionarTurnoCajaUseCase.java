package com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.AbrirTurnoCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.CerrarTurnoCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;

/**
 * Driving Port (Input Port): Caso de uso para la gestión transaccional de apertura y cierre de turnos.
 */
public interface GestionarTurnoCajaUseCase {

    TurnoCajaResponse abrirTurno(AbrirTurnoCommand command);

    TurnoCajaResponse cerrarTurno(CerrarTurnoCommand command);
}
