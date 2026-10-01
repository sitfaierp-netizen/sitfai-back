package com.SITFAI_CORE_ERP_TIENDA.core.idempotency.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.domain.model.EmpresaId;

/** Resolves the authenticated tenant for idempotent HTTP operations. */
public interface CurrentTenantPort {
    EmpresaId requireCurrentTenant();
}
