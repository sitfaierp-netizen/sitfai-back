package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.EmpresaId;

public interface TenantProviderPort {
    EmpresaId getEmpresaIdAutenticada();
}
