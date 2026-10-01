package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.security;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;
import org.springframework.stereotype.Component;

/**
 * Driven Adapter: Implementa TenantProviderPort usando Spring Security.
 * Asegura que el EmpresaId se extraiga criptográficamente del JWT (MT-01, MT-06).
 */
@Component("inventoryTenantProviderAdapter")
public class SpringSecurityTenantProviderAdapter implements TenantProviderPort {

    private final CurrentTenantProvider currentTenantProvider;

    public SpringSecurityTenantProviderAdapter(CurrentTenantProvider currentTenantProvider) {
        this.currentTenantProvider = currentTenantProvider;
    }

    @Override
    public EmpresaId getEmpresaIdAutenticada() {
        return new EmpresaId(currentTenantProvider.requireCurrentTenant());
    }
}
