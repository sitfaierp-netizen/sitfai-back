package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.exception;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.OrdenCompraId;

/**
 * Excepción lanzada cuando una Orden de Compra no existe para la Empresa consultada (MT-01).
 */
public class OrdenCompraNoEncontradaException extends PurchasingDomainException {

    public OrdenCompraNoEncontradaException(OrdenCompraId id, EmpresaId empresaId) {
        super(String.format("No se encontró la Orden de Compra [%s] para la empresa [%s].", id, empresaId));
    }

    public OrdenCompraNoEncontradaException(String message) {
        super(message);
    }
}
