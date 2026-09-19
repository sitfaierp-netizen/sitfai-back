package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.security;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityTenantProviderAdapter implements TenantProviderPort {

    @Override
    public EmpresaId getEmpresaIdAutenticada() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new SecurityException("No se encontró contexto de seguridad válido (MT-01)");
        }
        
        String empresaIdClaim = jwt.getClaimAsString("empresa_id");
        if (empresaIdClaim == null || empresaIdClaim.isBlank()) {
            throw new SecurityException("El token JWT no contiene el claim obligatorio 'empresa_id' (MT-01)");
        }
        
        return new EmpresaId(UUID.fromString(empresaIdClaim));
    }
}
