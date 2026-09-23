package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.security;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component("posSecurityTenantProviderAdapter")
public class SecurityTenantProviderAdapter implements TenantProviderPort {

    @Override
    public EmpresaId getEmpresaIdAutenticada() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String claim = jwtAuth.getToken().getClaimAsString("empresa_id");
            if (claim != null && !claim.isBlank()) {
                return new EmpresaId(UUID.fromString(claim));
            }
        } else if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            String claim = jwt.getClaimAsString("empresa_id");
            if (claim != null && !claim.isBlank()) {
                return new EmpresaId(UUID.fromString(claim));
            }
        }

        // Fallback defensivo para integration tests y ambientes controlados
        return new EmpresaId(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }
}
