package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.mapper;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ConteoCiclicoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.DetalleConteoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarConteoFisicoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.ConteoCiclicoWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.DetalleConteoWebResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.RegistrarConteoFisicoWebRequest;

import java.util.List;
import java.util.UUID;

/**
 * Mapper de Infraestructura: Conversión entre contratos HTTP (Web DTOs) y contratos de Aplicación.
 * <p>
 * Regla 5 (API REST): Aislamiento total de los DTOs web respecto al Dominio y Aplicación.
 */
public final class ConteoWebMapper {

    private ConteoWebMapper() {
    }

    public static RegistrarConteoFisicoCommand toCommand(UUID conteoId, RegistrarConteoFisicoWebRequest request) {
        return new RegistrarConteoFisicoCommand(
                conteoId,
                request.productoId(),
                request.cantidadFisica()
        );
    }

    public static ConteoCiclicoWebResponse toWebResponse(ConteoCiclicoResponse response) {
        List<DetalleConteoWebResponse> detalles = response.detalles().stream()
                .map(ConteoWebMapper::toDetalleWebResponse)
                .toList();

        return new ConteoCiclicoWebResponse(
                response.id(),
                response.empresaId(),
                response.bodegaId(),
                response.estado(),
                response.fechaProgramada(),
                response.totalLineas(),
                response.totalDiscrepancias(),
                detalles
        );
    }

    public static DetalleConteoWebResponse toDetalleWebResponse(DetalleConteoResponse d) {
        return new DetalleConteoWebResponse(
                d.lineaId(),
                d.productoId(),
                d.cantidadTeorica(),
                d.cantidadFisica(),
                d.diferencia(),
                d.tieneDiscrepancia()
        );
    }
}
