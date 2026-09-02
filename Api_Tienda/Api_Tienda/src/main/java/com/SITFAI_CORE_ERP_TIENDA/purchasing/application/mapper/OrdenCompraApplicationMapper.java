package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.mapper;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.OrdenCompra;

import java.math.BigDecimal;

public final class OrdenCompraApplicationMapper {

    private OrdenCompraApplicationMapper() {}

    public static OrdenCompraResponse aResponse(OrdenCompra orden) {
        return new OrdenCompraResponse(
                orden.getId().valor(),
                orden.getEmpresaId(),
                orden.getProveedorId().valor(),
                orden.getCreatedAt().toString(),
                orden.getEstado().name(),
                orden.getTotalMonetario().monto(),
                orden.getLineas().stream().map(l -> new OrdenCompraResponse.LineaResponse(
                        l.getProductoId().valor(),
                        BigDecimal.valueOf(l.getCantidad()),
                        l.getPrecioUnitario().monto(),
                        l.getSubtotal().monto()
                )).toList()
        );
    }
}
