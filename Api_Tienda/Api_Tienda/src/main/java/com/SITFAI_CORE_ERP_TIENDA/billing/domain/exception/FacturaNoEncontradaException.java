package com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.FacturaId;

/**
 * Excepción lanzada cuando una Factura no existe o no pertenece al Tenant (MT-01, MT-02).
 */
public class FacturaNoEncontradaException extends DomainException {

    public FacturaNoEncontradaException(FacturaId facturaId, EmpresaId empresaId) {
        super(String.format("Factura '%s' no encontrada para la empresa '%s'.", facturaId, empresaId));
    }
}
