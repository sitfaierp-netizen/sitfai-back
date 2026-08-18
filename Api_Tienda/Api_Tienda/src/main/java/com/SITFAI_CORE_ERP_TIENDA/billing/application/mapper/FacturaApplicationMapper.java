package com.SITFAI_CORE_ERP_TIENDA.billing.application.mapper;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.FacturaElectronica;

/**
 * Mapper Utilitario para Factura Electrónica.
 * Convierte el Agregado del Dominio a DTO de Respuesta.
 */
public final class FacturaApplicationMapper {

    private FacturaApplicationMapper() {
        // Utility class
    }

    public static FacturaResponse aResponse(FacturaElectronica factura) {
        return new FacturaResponse(
                factura.getId().valor(),
                factura.getEmpresaId().valor(),
                factura.getNitEmisor().valor(),
                factura.getNitReceptor().valor(),
                factura.getEstado().name(),
                factura.getCufe() != null ? factura.getCufe().valor() : null,
                factura.getSubtotal().monto(),
                factura.getTotalImpuestos().monto(),
                factura.getTotalGeneral().monto(),
                factura.getLineas().stream().map(l -> new FacturaResponse.LineaFacturaResponse(
                        l.getConcepto(),
                        l.getCantidad(),
                        l.getPrecioUnitario().monto(),
                        l.calcularSubtotal().monto(),
                        l.calcularTotalImpuestos().monto()
                )).toList()
        );
    }
}
