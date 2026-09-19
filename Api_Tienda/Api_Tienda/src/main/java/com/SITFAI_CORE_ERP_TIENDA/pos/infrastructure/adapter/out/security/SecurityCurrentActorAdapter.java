package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.security;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.CurrentActorProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityCurrentActorAdapter implements CurrentActorProvider {

    @Override
    public String getActorActual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return "SYSTEM_POS_MODULE"; // Fallback defensivo si no hay contexto (por ejemplo, en tests) o lanzar excepción.
        }
        return authentication.getName();
    }
}
