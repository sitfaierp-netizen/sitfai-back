package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.mapper;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.LineaDespachoResponse;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.dto.OrdenDespachoResponse;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.LineaDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.OrdenDespacho;

import java.util.List;
import java.util.stream.Collectors;

public class OrdenDespachoApplicationMapper {

    private OrdenDespachoApplicationMapper() {
    }

    public static OrdenDespachoResponse toResponse(OrdenDespacho orden) {
        if (orden == null) {
            return null;
        }

        List<LineaDespachoResponse> lineasResponse = orden.getLineas().stream()
                .map(OrdenDespachoApplicationMapper::toResponse)
                .collect(Collectors.toList());

        return new OrdenDespachoResponse(
                orden.getId().value(),
                orden.getEmpresaId().value(),
                orden.getPedidoOrigenId().value(),
                orden.getEstado().name(),
                orden.getDireccionEntrega().direccionLocal(),
                orden.getDireccionEntrega().ciudad(),
                orden.getDireccionEntrega().codigoPostal(),
                lineasResponse
        );
    }

    private static LineaDespachoResponse toResponse(LineaDespacho linea) {
        return new LineaDespachoResponse(
                linea.getId(),
                linea.getProductoId().value(),
                linea.getCantidadSolicitada().value(),
                linea.getCantidadPreparada().value()
        );
    }
}
