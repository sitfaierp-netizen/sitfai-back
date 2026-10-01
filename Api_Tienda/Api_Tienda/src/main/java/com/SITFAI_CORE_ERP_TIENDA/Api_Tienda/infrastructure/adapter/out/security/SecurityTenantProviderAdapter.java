package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.security;

import org.springframework.stereotype.Component;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.CurrentActorProvider;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Component("apiTiendaSecurityTenantProviderAdapter")
public class SecurityTenantProviderAdapter implements TenantProviderPort, CurrentActorProvider {

    private final CurrentTenantProvider currentTenantProvider;

    public SecurityTenantProviderAdapter(CurrentTenantProvider currentTenantProvider) {
        this.currentTenantProvider = currentTenantProvider;
    }

    @Override
    public EmpresaId getEmpresaIdAutenticada() {
        return new EmpresaId(currentTenantProvider.requireCurrentTenant());
    }

    @Override
    public String getActorActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            String actorIdStr = jwtToken.getToken().getSubject();
            if (actorIdStr != null) {
                return actorIdStr;
            }
        }
        return "00000000-0000-0000-0000-000000000000";
    }
}
