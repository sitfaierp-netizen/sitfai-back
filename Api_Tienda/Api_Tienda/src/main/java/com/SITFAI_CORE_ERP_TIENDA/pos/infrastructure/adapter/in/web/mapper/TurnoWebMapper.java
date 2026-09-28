package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.mapper;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.AbrirTurnoCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.ArqueoResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.CerrarTurnoCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.RegistrarTransaccionCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TransaccionResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.AbrirTurnoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.ArqueoWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.RegistrarTransaccionWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.TransaccionWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.TurnoWebResponse;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Web Mapper: Mapeador bidireccional entre Payloads REST (Web DTOs) y Commands/Responses de Aplicación (REGLA-5).
 */
public final class TurnoWebMapper {

    private TurnoWebMapper() {
        // Utility class
    }

    public static AbrirTurnoCommand toCommand(UUID empresaId, AbrirTurnoWebRequest request) {
        Objects.requireNonNull(empresaId, "empresaId es obligatorio.");
        Objects.requireNonNull(request, "request es obligatorio.");

        return new AbrirTurnoCommand(
                empresaId,
                request.cajaId(),
                request.sucursalId(),
                request.CajeroId(),
                request.montoApertura()
        );
    }

    public static RegistrarTransaccionCommand toCommand(
            UUID empresaId,
            UUID turnoId,
            RegistrarTransaccionWebRequest request) {
        Objects.requireNonNull(empresaId, "empresaId es obligatorio.");
        Objects.requireNonNull(turnoId, "turnoId es obligatorio.");
        Objects.requireNonNull(request, "request es obligatorio.");

        return new RegistrarTransaccionCommand(
                empresaId,
                turnoId,
                request.tipoTransaccion(),
                request.monto(),
                request.referencia()
        );
    }

    public static CerrarTurnoCommand toCerrarCommand(UUID empresaId, UUID turnoId) {
        Objects.requireNonNull(empresaId, "empresaId es obligatorio.");
        Objects.requireNonNull(turnoId, "turnoId es obligatorio.");

        return new CerrarTurnoCommand(empresaId, turnoId, null); // montoCierreDeclarado can be null or handled differently
    }

    public static TurnoWebResponse toWebResponse(TurnoResponse response) {
        if (response == null) {
            return null;
        }

        List<TransaccionWebResponse> transaccionesWeb = response.transacciones() != null
                ? response.transacciones().stream().map(TurnoWebMapper::toWebResponse).toList()
                : Collections.emptyList();

        ArqueoWebResponse arqueoWeb = response.arqueo() != null
                ? toWebResponse(response.arqueo())
                : null;

        return new TurnoWebResponse(
                response.id(),
                response.cajaId(),
                response.empresaId(),
                response.estado(),
                response.montoInicial(),
                response.moneda(),
                transaccionesWeb,
                arqueoWeb,
                response.abiertoEn(),
                response.cerradoEn(),
                response.creadoEn(),
                response.actualizadoEn()
        );
    }

    public static TransaccionWebResponse toWebResponse(TransaccionResponse response) {
        if (response == null) {
            return null;
        }

        return new TransaccionWebResponse(
                response.id(),
                response.TipoTransaccionCaja(),
                response.monto(),
                response.moneda(),
                response.concepto(),
                response.registradoEn()
        );
    }

    public static ArqueoWebResponse toWebResponse(ArqueoResponse response) {
        if (response == null) {
            return null;
        }

        return new ArqueoWebResponse(
                response.montoInicial(),
                response.totalVentas(),
                response.totalDevoluciones(),
                response.totalIngresos(),
                response.totalEgresos(),
                response.balanceEsperado(),
                response.moneda()
        );
    }
}
