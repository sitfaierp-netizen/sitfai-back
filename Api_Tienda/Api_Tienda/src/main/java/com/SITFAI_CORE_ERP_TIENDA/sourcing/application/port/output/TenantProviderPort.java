package com.SITFAI_CORE_ERP_TIENDA.sourcing.application.port.output;

import java.util.UUID;

/** Driven Port para obtener el EmpresaId (MT-01) del contexto de seguridad. */
public interface TenantProviderPort {
    UUID obtenerEmpresaIdActual();
}
