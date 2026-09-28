package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;

/**
 * Puerto de Salida para obtener el Tenant actual (EmpresaId).
 * Sigue el patrÃ³n MT-01.
 */
public interface TenantProviderPort {
    EmpresaId getEmpresaIdAutenticada();
}

