package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.exception;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.EmpresaId;

public class DespachoNoEncontradoException extends RuntimeException {
    public DespachoNoEncontradoException(DespachoId despachoId, EmpresaId empresaId) {
        super("Despacho " + despachoId.value() + " no encontrado para la empresa " + empresaId.value());
    }
}
