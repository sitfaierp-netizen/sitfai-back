package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.security;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;
import org.springframework.stereotype.Component;

@Component("productionSecurityTenantProviderAdapter")
public class SecurityTenantProviderAdapter implements TenantProviderPort {

    private final CurrentTenantProvider currentTenantProvider;

    public SecurityTenantProviderAdapter(CurrentTenantProvider currentTenantProvider) {
        this.currentTenantProvider = currentTenantProvider;
    }

    @Override
    public EmpresaId getEmpresaIdAutenticada() {
        return new EmpresaId(currentTenantProvider.requireCurrentTenant());
    }
}
