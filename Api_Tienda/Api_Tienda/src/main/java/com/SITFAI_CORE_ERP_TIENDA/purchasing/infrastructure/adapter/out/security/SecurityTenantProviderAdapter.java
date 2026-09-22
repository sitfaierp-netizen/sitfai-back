package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.security;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.CurrentActorProvider;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.EmpresaId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Driven Adapter: Resuelve el Tenant actual (MT-01) y el Actor (AUD-01)
 * a partir del SecurityContextHolder de Spring Security y tokens JWT.
 */
@Component("purchasingSecurityTenantProviderAdapter")
public class SecurityTenantProviderAdapter implements TenantProviderPort, CurrentActorProvider {

    @Override
    public EmpresaId getEmpresaIdAutenticada() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            String empresaIdStr = jwtToken.getToken().getClaimAsString("empresa_id");
            if (empresaIdStr != null && !empresaIdStr.isBlank()) {
                return EmpresaId.de(UUID.fromString(empresaIdStr));
            }
        }
        // Fallback defensivo para tests de integración y ejecuciones internas
        return EmpresaId.de(UUID.fromString("00000000-0000-0000-0000-000000000000"));
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
