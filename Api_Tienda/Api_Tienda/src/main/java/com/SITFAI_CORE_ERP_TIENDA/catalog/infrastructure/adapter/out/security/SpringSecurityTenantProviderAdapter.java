package com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.security;

import com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security.TenantAuthenticationDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Driven Adapter: Implementa TenantProviderPort usando Spring Security.
 * Asegura que el EmpresaId se extraiga criptográficamente del JWT (MT-01, MT-06).
 */
@Component
public class SpringSecurityTenantProviderAdapter implements TenantProviderPort {

    @Override
    public UUID obtenerEmpresaIdActual() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No hay un usuario autenticado en el contexto de seguridad.");
        }

        Object details = auth.getDetails();
        if (details instanceof TenantAuthenticationDetails tenantDetails) {
            return tenantDetails.empresaUuid();
        }

        throw new IllegalStateException(
                "No se encontraron detalles de tenant (TenantAuthenticationDetails) en la autenticación actual.");
    }
}
