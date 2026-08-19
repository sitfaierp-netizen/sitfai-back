package com.SITFAI_CORE_ERP_TIENDA.catalog.application.port.output;

import java.util.UUID;

/**
 * Driven Port: Obtiene el EmpresaId del contexto de seguridad activo (MT-01, MT-06).
 * El Application Service lo invoca; la implementación usa Spring Security en Infrastructure.
 * El Dominio NO conoce este puerto.
 */
public interface TenantProviderPort {

    /**
     * Extrae el UUID del tenant (empresa) del contexto de seguridad de la request actual.
     *
     * @return UUID del tenant autenticado.
     * @throws IllegalStateException si no hay contexto de seguridad activo.
     */
    UUID obtenerEmpresaIdActual();
}
