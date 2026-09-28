package com.SITFAI_CORE_ERP_TIENDA.pos.application.mapper;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TransaccionCajaResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.TransaccionCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.TurnoCaja;

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
                null,
                turno.getCajeroId().value(),
                turno.getEstado().name(),
                turno.getMontoApertura().monto(),
                turno.calcularTotalTeorico().monto(),
                transaccionesResponse
        );
    }

    private static TransaccionCajaResponse toResponse(TransaccionCaja transaccion) {
        return new TransaccionCajaResponse(
                transaccion.getId(),
                transaccion.getTipo().name(),
                transaccion.getMonto().monto(),
                transaccion.getDocumentoFuenteId(),
                transaccion.getFechaHora()
        );
    }
}
