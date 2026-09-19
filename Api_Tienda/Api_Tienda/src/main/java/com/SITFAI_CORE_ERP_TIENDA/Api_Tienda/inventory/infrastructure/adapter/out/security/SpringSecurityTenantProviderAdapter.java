package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.security;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security.TenantAuthenticationDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

/**
 * Driven Adapter: Implementa TenantProviderPort usando Spring Security.
 * Asegura que el EmpresaId se extraiga criptográficamente del JWT (MT-01, MT-06).
 */
@Component("inventoryTenantProviderAdapter")
public class SpringSecurityTenantProviderAdapter implements TenantProviderPort {

    @Override
    public EmpresaId getEmpresaIdAutenticada() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No hay un usuario autenticado en el contexto de seguridad.");
        }

        Object details = auth.getDetails();
        if (details instanceof TenantAuthenticationDetails tenantDetails) {
            return new EmpresaId(tenantDetails.empresaUuid());
        }

        boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
        
        if (isSuperAdmin) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String headerEmpresaId = attributes.getRequest().getHeader("X-Empresa-Id");
                if (headerEmpresaId != null && !headerEmpresaId.isBlank()) {
                    return new EmpresaId(UUID.fromString(headerEmpresaId.trim()));
                }
            }
        }

        throw new IllegalStateException(
                "No se encontraron detalles de tenant (TenantAuthenticationDetails) en la autenticación actual.");
    }
}
