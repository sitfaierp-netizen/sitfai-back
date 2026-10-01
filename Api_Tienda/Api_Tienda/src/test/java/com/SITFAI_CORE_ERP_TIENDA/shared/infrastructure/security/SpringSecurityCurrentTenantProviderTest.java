package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpringSecurityCurrentTenantProviderTest {

    private final SpringSecurityCurrentTenantProvider provider = new SpringSecurityCurrentTenantProvider();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void resolvesTenantOnlyFromAuthenticatedDetails() {
        UUID tenant = UUID.randomUUID();
        authenticate(tenant, "ROLE_EMPRESA_ADMIN");

        assertThat(provider.requireCurrentTenant()).isEqualTo(tenant);
        assertThat(provider.authorizeTenant(tenant)).isEqualTo(tenant);
    }

    @Test
    void rejectsHorizontalTenantSelection() {
        authenticate(UUID.randomUUID(), "ROLE_EMPRESA_ADMIN");

        assertThatThrownBy(() -> provider.authorizeTenant(UUID.randomUUID()))
                .isInstanceOf(TenantScopeViolationException.class);
    }

    @Test
    void allowsExplicitCrossTenantSelectionOnlyForGlobalAdministrator() {
        UUID requestedTenant = UUID.randomUUID();
        authenticate(null, "ROLE_SUPER_ADMIN");

        assertThat(provider.authorizeTenant(requestedTenant)).isEqualTo(requestedTenant);
        assertThat(provider.isGlobalAdministrator()).isTrue();
    }

    @Test
    void failsClosedWithoutAuthenticatedTenant() {
        assertThatThrownBy(provider::requireCurrentTenant)
                .isInstanceOf(TenantScopeViolationException.class);
    }

    private void authenticate(UUID tenant, String authority) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(
                "principal",
                "credentials",
                List.of(new SimpleGrantedAuthority(authority))
        );
        authentication.setAuthenticated(true);
        if (tenant != null) {
            authentication.setDetails(new TenantAuthenticationDetails(tenant.toString()));
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
