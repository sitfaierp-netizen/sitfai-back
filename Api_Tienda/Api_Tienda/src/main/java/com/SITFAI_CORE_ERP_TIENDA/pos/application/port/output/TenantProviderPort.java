package com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EmpresaId;

/**
 * Puerto de salida para obtener la empresa autenticada.
 * Garantiza el cumplimiento de MT-01 sin depender del payload.
 */
public interface TenantProviderPort {
    EmpresaId getEmpresaIdAutenticada();
}
