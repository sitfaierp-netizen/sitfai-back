package com.SITFAI_CORE_ERP_TIENDA.billing.application.mapper;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.Factura;

import java.util.stream.Collectors;

public final class FacturaApplicationMapper {

    private FacturaApplicationMapper() {}

    public static FacturaResponse toResponse(Factura factura) {
        return new FacturaResponse(
                factura.getId().value().toString(),
                factura.getEmpresaId().toString(),
                factura.getClienteId().value().toString(),
                factura.getPedidoId() != null ? factura.getPedidoId().value().toString() : null,
                factura.getRucCliente().valor(),
                factura.getSubtotal().monto(),
                factura.getTotalImpuestos().monto(),
                factura.getTotalGeneral().monto(),
                factura.getEstado().name(),
                factura.getLineas().stream()
                        .map(l -> new FacturaResponse.LineaFacturaResponse(
                                l.getConcepto(),
                                l.getCantidad(),
                                l.getPrecioUnitario().monto(),
                                l.calcularSubtotal().monto(),
                                l.calcularTotalImpuestos().monto()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
