package com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityActorProviderTest {

    private final SpringSecurityActorProviderAdapter provider = new SpringSecurityActorProviderAdapter();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void debeRetornarSystemSiNoHayAutenticacion() {
        assertEquals("system", provider.getCurrentActorId());
    }

    @Test
    void debeExtraerPreferredUsernameDeJwt() {
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaimAsString("preferred_username")).thenReturn("admin_user");
        
        JwtAuthenticationToken token = new JwtAuthenticationToken(jwt);
        token.setAuthenticated(true);
        
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(token);
        SecurityContextHolder.setContext(context);

        assertEquals("admin_user", provider.getCurrentActorId());
    }
}
