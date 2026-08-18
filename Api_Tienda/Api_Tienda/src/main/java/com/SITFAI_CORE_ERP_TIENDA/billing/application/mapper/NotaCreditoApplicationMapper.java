package com.SITFAI_CORE_ERP_TIENDA.billing.application.mapper;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.NotaCreditoResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.NotaCreditoElectronica;

public final class NotaCreditoApplicationMapper {

    private NotaCreditoApplicationMapper() {}

    public static NotaCreditoResponse aResponse(NotaCreditoElectronica nota) {
        return new NotaCreditoResponse(
                nota.getId().valor(),
                nota.getEmpresaId().valor(),
                nota.getFacturaAfectadaId().valor(),
                nota.getEstado().name(),
                nota.getCufe() != null ? nota.getCufe().valor() : null,
                nota.getSubtotal().monto(),
                nota.getTotalImpuestos().monto(),
                nota.getTotalGeneral().monto(),
                nota.getLineasReversadas().stream().map(l -> new NotaCreditoResponse.LineaReversoResponse(
                        l.getConcepto(),
                        l.getCantidad(),
                        l.getPrecioUnitario().monto(),
                        l.calcularSubtotal().monto(),
                        l.calcularTotalImpuestos().monto()
                )).toList()
        );
    }
}
