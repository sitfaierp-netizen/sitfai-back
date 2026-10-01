package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.adapter.out.security;

import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.application.port.output.CurrentTenantPort;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;
import org.springframework.stereotype.Component;

import java.util.Objects;

/** Adapts the shared authenticated tenant source to the idempotency context. */
@Component
public class SecurityCurrentTenantAdapter implements CurrentTenantPort {

    private final CurrentTenantProvider currentTenantProvider;

    public SecurityCurrentTenantAdapter(CurrentTenantProvider currentTenantProvider) {
        this.currentTenantProvider = Objects.requireNonNull(currentTenantProvider);
    }

    @Override
    public EmpresaId requireCurrentTenant() {
        return EmpresaId.de(currentTenantProvider.requireCurrentTenant());
    }
}
