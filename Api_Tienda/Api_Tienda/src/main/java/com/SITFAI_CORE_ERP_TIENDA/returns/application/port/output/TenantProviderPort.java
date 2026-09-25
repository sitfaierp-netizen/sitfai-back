package com.SITFAI_CORE_ERP_TIENDA.returns.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.EmpresaId;

public interface TenantProviderPort {
    EmpresaId getEmpresaIdAutenticada();
}
