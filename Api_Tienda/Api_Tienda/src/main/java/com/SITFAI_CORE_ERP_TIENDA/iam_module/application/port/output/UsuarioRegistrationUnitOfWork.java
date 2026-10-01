package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.port.output;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.Usuario;

/**
 * Límites transaccionales locales del alta de usuario. Cada operación confirma
 * su transacción antes de devolver el control al coordinador de identidad.
 */
public interface UsuarioRegistrationUnitOfWork {

    Usuario guardarPendiente(Usuario usuario);

    Usuario confirmarIdentidad(Usuario usuario);
}
