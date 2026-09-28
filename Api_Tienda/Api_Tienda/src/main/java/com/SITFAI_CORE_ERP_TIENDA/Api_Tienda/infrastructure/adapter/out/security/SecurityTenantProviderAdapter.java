package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.CurrentActorProvider;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.UUID;

@Component("apiTiendaSecurityTenantProviderAdapter")
public class SecurityTenantProviderAdapter implements TenantProviderPort, CurrentActorProvider {

    @Override
    public EmpresaId getEmpresaIdAutenticada() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            String empresaIdStr = jwtToken.getToken().getClaimAsString("empresa_id");
            if (empresaIdStr != null) {
                return new EmpresaId(UUID.fromString(empresaIdStr));
            }
        }
        // Fallback for E2E tests, usually the Big Bang creates the first company
        return new EmpresaId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
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

