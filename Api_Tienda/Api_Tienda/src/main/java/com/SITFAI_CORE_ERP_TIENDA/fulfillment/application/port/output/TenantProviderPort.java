package com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.EmpresaId;

public interface TenantProviderPort {
    EmpresaId getEmpresaIdAutenticada();
}
