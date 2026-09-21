package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;

/**
 * Puerto de Salida para obtener el Tenant actual (EmpresaId).
 * Sigue el patrón MT-01.
 */
public interface TenantProviderPort {
    EmpresaId getEmpresaIdAutenticada();
}
