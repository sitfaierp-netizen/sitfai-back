package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security;

import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Canonical tenant source. Only claims from an authenticated JWT (or the details
 * produced by the JWT converter) are trusted.
 */
@Component
public class SpringSecurityCurrentTenantProvider implements CurrentTenantProvider {

    private static final String GLOBAL_ADMIN_AUTHORITY = "ROLE_SUPER_ADMIN";

    @Override
    public UUID requireCurrentTenant() {
        return currentTenant().orElseThrow(TenantScopeViolationException::new);
    }

    @Override
    public UUID authorizeTenant(UUID requestedTenant) {
        if (requestedTenant == null) {
            return requireCurrentTenant();
        }

        Optional<UUID> authenticatedTenant = currentTenant();
        if (authenticatedTenant.filter(requestedTenant::equals).isPresent()) {
            return requestedTenant;
        }
        if (isGlobalAdministrator()) {
            return requestedTenant;
        }
        throw new TenantScopeViolationException();
    }

    @Override
    public boolean isGlobalAdministrator() {
        Authentication authentication = authenticatedPrincipal();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> GLOBAL_ADMIN_AUTHORITY.equals(authority.getAuthority()));
    }

    private Optional<UUID> currentTenant() {
        Authentication authentication = authenticatedPrincipal();
        if (authentication == null) {
            return Optional.empty();
        }

        if (authentication.getDetails() instanceof TenantAuthenticationDetails details) {
            return parse(details.empresaId());
        }
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return parse(extractTenantClaim(jwtAuthentication.getToken()));
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return parse(extractTenantClaim(jwt));
        }
        return Optional.empty();
    }

    private Authentication authenticatedPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() ? authentication : null;
    }

    private String extractTenantClaim(Jwt jwt) {
        String claim = jwt.getClaimAsString(KeycloakJwtAuthenticationConverter.CLAIM_EMPRESA_ID);
        if (claim == null || claim.isBlank()) {
            claim = jwt.getClaimAsString(KeycloakJwtAuthenticationConverter.CLAIM_EMPRESA_ID_ALT);
        }
        return claim;
    }

    private Optional<UUID> parse(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(UUID.fromString(value.trim()));
        } catch (IllegalArgumentException exception) {
            throw new TenantScopeViolationException(exception);
        }
    }
}
