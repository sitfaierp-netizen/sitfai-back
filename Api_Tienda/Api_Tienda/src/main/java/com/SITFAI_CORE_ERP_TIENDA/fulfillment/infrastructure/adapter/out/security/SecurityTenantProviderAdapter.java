package com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.security;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;
import org.springframework.stereotype.Component;

@Component("fulfillmentSecurityTenantProviderAdapter")
public class SecurityTenantProviderAdapter implements TenantProviderPort {

    private final CurrentTenantProvider currentTenantProvider;

    public SecurityTenantProviderAdapter(CurrentTenantProvider currentTenantProvider) {
        this.currentTenantProvider = currentTenantProvider;
    }

    @Override
    public EmpresaId getEmpresaIdAutenticada() {
        return EmpresaId.de(currentTenantProvider.requireCurrentTenant());
    }
}
