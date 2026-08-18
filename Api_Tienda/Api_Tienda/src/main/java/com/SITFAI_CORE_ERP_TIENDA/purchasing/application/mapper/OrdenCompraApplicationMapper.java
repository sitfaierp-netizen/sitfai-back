package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.mapper;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;

public final class OrdenCompraApplicationMapper {

    private OrdenCompraApplicationMapper() {}

    public static OrdenCompraResponse aResponse(OrdenCompra orden) {
        return new OrdenCompraResponse(
                orden.getId().valor(),
                orden.getEmpresaId().valor(),
                orden.getProveedorId().valor(),
                orden.getFechaCreacion().toString(),
                orden.getEstado().name(),
                orden.getCostoTotal().monto(),
                orden.getLineas().stream().map(l -> new OrdenCompraResponse.LineaResponse(
                        l.getProductoId().valor(),
                        l.getCantidadSolicitada(),
                        l.getCostoUnitarioPactado().monto(),
                        l.calcularSubtotal().monto()
                )).toList()
        );
    }
}
