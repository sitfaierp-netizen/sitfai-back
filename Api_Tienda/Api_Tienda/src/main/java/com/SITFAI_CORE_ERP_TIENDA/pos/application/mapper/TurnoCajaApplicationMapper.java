package com.SITFAI_CORE_ERP_TIENDA.pos.application.mapper;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TransaccionCajaResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TransaccionCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja;

import java.util.List;
import java.util.stream.Collectors;

public class TurnoCajaApplicationMapper {

    private TurnoCajaApplicationMapper() {
    }

    public static TurnoCajaResponse toResponse(TurnoCaja turno) {
        if (turno == null) {
            return null;
        }

        List<TransaccionCajaResponse> transaccionesResponse = turno.getTransacciones().stream()
                .map(TurnoCajaApplicationMapper::toResponse)
                .collect(Collectors.toList());

        return new TurnoCajaResponse(
                turno.getId().value(),
                turno.getEmpresaId().value(),
                turno.getCajaId().value(),
                turno.getSucursalId().value(),
                turno.getUsuarioId().value(),
                turno.getEstado().name(),
                turno.getMontoApertura().valor(),
                turno.calcularConsolidado().valor(),
                transaccionesResponse
        );
    }

    private static TransaccionCajaResponse toResponse(TransaccionCaja transaccion) {
        return new TransaccionCajaResponse(
                transaccion.getId(),
                transaccion.getTipo().name(),
                transaccion.getMonto().valor(),
                transaccion.getReferencia(),
                transaccion.getFecha()
        );
    }
}
