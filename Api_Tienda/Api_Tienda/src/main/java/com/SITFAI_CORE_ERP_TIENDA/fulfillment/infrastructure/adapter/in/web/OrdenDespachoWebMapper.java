package com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.CompletarPackingCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.LineaDespachoCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.PlanificarDespachoCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.RegistrarPickingCommand;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.in.web.dto.PlanificarDespachoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.in.web.dto.RegistrarPickingWebRequest;

import java.util.UUID;
import java.util.stream.Collectors;

public class OrdenDespachoWebMapper {

    private OrdenDespachoWebMapper() {}

    public static PlanificarDespachoCommand toCommand(UUID empresaId, PlanificarDespachoWebRequest request) {
        return new PlanificarDespachoCommand(
                empresaId,
                request.pedidoOrigenId(),
                request.direccionLocal(),
                request.ciudad(),
                request.codigoPostal(),
                request.lineas().stream()
                        .map(l -> new LineaDespachoCommand(l.productoId(), l.cantidadSolicitada()))
                        .collect(Collectors.toList())
        );
    }

    public static RegistrarPickingCommand toCommand(UUID empresaId, UUID despachoId, RegistrarPickingWebRequest request) {
        return new RegistrarPickingCommand(
                empresaId,
                despachoId,
                request.productoId(),
                request.cantidadPreparada()
        );
    }

    public static CompletarPackingCommand toCompletarCommand(UUID empresaId, UUID despachoId) {
        return new CompletarPackingCommand(empresaId, despachoId);
    }
}
