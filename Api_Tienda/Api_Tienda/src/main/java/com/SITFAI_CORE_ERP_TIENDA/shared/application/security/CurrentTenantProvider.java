package com.SITFAI_CORE_ERP_TIENDA.shared.application.security;

import java.util.UUID;

/**
 * Shared application port for resolving the authenticated tenant scope.
 * Implementations must only trust validated authentication data.
 */
public interface CurrentTenantProvider {

    UUID requireCurrentTenant();

    UUID authorizeTenant(UUID requestedTenant);

    boolean isGlobalAdministrator();
}
