package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Usuario;

/**
 * Puerto de salida para mantener la identidad externa alineada con el usuario local.
 * Todas las operaciones deben ser idempotentes para permitir reintentos seguros.
 */
public interface IdentityProvisioningPort {

    void provisionar(Usuario usuario);

    void completarOnboarding(Usuario usuario);

    void sincronizarRol(Usuario usuario);

    void sincronizarEstado(Usuario usuario);

    void reconciliar(Usuario usuario);
}
