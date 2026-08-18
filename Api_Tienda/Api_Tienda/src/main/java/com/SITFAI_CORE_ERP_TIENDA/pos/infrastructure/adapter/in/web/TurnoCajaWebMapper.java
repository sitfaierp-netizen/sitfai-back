package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.AbrirTurnoCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.CerrarTurnoCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.RegistrarTransaccionCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.AbrirTurnoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.CerrarTurnoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.RegistrarTransaccionWebRequest;

import java.util.UUID;

public class TurnoCajaWebMapper {

    private TurnoCajaWebMapper() {}

    public static AbrirTurnoCommand toCommand(UUID empresaId, AbrirTurnoWebRequest request) {
        return new AbrirTurnoCommand(
                empresaId,
                request.cajaId(),
                request.sucursalId(),
                request.usuarioId(),
                request.montoApertura()
        );
    }

    public static RegistrarTransaccionCommand toCommand(UUID empresaId, UUID turnoId, RegistrarTransaccionWebRequest request) {
        return new RegistrarTransaccionCommand(
                empresaId,
                turnoId,
                request.tipoTransaccion(),
                request.monto(),
                request.referencia()
        );
    }

    public static CerrarTurnoCommand toCommand(UUID empresaId, UUID turnoId, CerrarTurnoWebRequest request) {
        return new CerrarTurnoCommand(
                empresaId,
                turnoId,
                request.montoDeclarado()
        );
    }
}
