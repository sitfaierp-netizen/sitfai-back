package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.security;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.CurrentActorProvider;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Driven Adapter: Resuelve el Tenant actual (MT-01) y el Actor (AUD-01)
 * a partir del SecurityContextHolder de Spring Security y tokens JWT.
 */
@Component("purchasingSecurityTenantProviderAdapter")
public class SecurityTenantProviderAdapter implements TenantProviderPort, CurrentActorProvider {

    private final CurrentTenantProvider currentTenantProvider;

    public SecurityTenantProviderAdapter(CurrentTenantProvider currentTenantProvider) {
        this.currentTenantProvider = currentTenantProvider;
    }

    @Override
    public EmpresaId getEmpresaIdAutenticada() {
        return EmpresaId.de(currentTenantProvider.requireCurrentTenant());
    }

    @Override
    public String getActorActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            String sub = jwtToken.getToken().getSubject();
            if (sub != null && !sub.isBlank()) {
                return sub;
            }
        } else if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null) {
            return authentication.getName();
        }
        return "SYSTEM_PURCHASING";
    }
}
