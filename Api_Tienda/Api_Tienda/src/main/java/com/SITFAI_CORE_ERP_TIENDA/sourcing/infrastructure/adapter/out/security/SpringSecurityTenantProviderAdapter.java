package com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.adapter.out.security;

import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.application.port.output.TenantProviderPort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("sourcingTenantProviderAdapter")
public class SpringSecurityTenantProviderAdapter implements TenantProviderPort {

    private final CurrentTenantProvider currentTenantProvider;

    public SpringSecurityTenantProviderAdapter(CurrentTenantProvider currentTenantProvider) {
        this.currentTenantProvider = currentTenantProvider;
    }

    @Override
    public UUID obtenerEmpresaIdActual() {
        return currentTenantProvider.requireCurrentTenant();
    }
}
