package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.EmpresaId;

/**
 * Driven / Output Port: Obtiene el identificador del Tenant (EmpresaId) autenticado en el contexto.
 * <p>
 * Regla MT-01: Prohibido confiar en el payload del cliente; el tenant siempre se extrae de la identidad segura.
 */
public interface TenantProviderPort {
    EmpresaId getEmpresaIdAutenticada();
}
