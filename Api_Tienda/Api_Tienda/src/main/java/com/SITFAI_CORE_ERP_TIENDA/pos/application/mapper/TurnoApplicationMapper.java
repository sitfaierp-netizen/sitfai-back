package com.SITFAI_CORE_ERP_TIENDA.pos.application.mapper;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.ArqueoResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TransaccionResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TransaccionCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.ArqueoCaja;

import java.util.List;
import java.util.Objects;

/**
 * Mapper estático de la Capa de Aplicación (Application Mapper): transforma Agregados y Entidades del Dominio
 * de POS a DTOs de salida inmutables (REGLA-1, MCP-01).
 * <p>
 * Clase utilitaria sin estado.
 */
public final class TurnoApplicationMapper {

    private TurnoApplicationMapper() {
        // Utility class
    }

    public static TurnoResponse toResponse(TurnoCaja turno) {
        Objects.requireNonNull(turno, "TurnoApplicationMapper: turno no puede ser null.");

        List<TransaccionResponse> transaccionesDto = turno.getTransacciones().stream()
                .map(TurnoApplicationMapper::toTransaccionResponse)
                .toList();

        ArqueoResponse arqueoDto = null;

        return new TurnoResponse(
                turno.getId().value(),
                turno.getCajaId().value(),
                turno.getEmpresaId().value(),
                turno.getEstado().name(),
                turno.getMontoApertura() != null ? turno.getMontoApertura().valor() : null,
                "USD", // moneda mock
                transaccionesDto,
                null, // arqueoDto no existe en TurnoCaja actual
                java.time.Instant.now(), // abiertoEn (mock)
                null, // cerradoEn (mock)
                java.time.Instant.now(), // creadoEn (mock)
                java.time.Instant.now() // actualizadoEn (mock)
        );
    }

    public static TransaccionResponse toTransaccionResponse(TransaccionCaja transaccion) {
        Objects.requireNonNull(transaccion, "TurnoApplicationMapper: transaccion no puede ser null.");

        return new TransaccionResponse(
                transaccion.getId(),
                transaccion.getTipo().name(),
                transaccion.getMonto().valor(),
                "USD",
                transaccion.getReferencia(),
                transaccion.getFecha()
        );
    }

    public static ArqueoResponse toArqueoResponse(ArqueoCaja arqueo) {
        if (arqueo == null) {
            return null;
        }

        return new ArqueoResponse(
                arqueo.montoInicial().valor(),
                arqueo.totalVentas().valor(),
                arqueo.totalDevoluciones().valor(),
                arqueo.totalIngresos().valor(),
                arqueo.totalEgresos().valor(),
                arqueo.balanceEsperado().valor(),
                "USD"
        );
    }
}
