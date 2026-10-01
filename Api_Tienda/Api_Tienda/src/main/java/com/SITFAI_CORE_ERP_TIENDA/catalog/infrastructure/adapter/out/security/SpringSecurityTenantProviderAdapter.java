package com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.security;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Driven Adapter: Implementa TenantProviderPort usando Spring Security.
 * Asegura que el EmpresaId se extraiga criptográficamente del JWT (MT-01, MT-06).
 */
@Component
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
