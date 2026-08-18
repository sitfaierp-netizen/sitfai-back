package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;

/**
 * Excepción lanzada cuando no se encuentra un Usuario bajo un tenant específico.
 */
public class UsuarioNoEncontradoException extends DomainException {

    public UsuarioNoEncontradoException(UsuarioId usuarioId, EmpresaId empresaId) {
        super(String.format("Usuario con ID '%s' no encontrado para la empresa '%s'.",
                usuarioId != null ? usuarioId.valor() : "null",
                empresaId != null ? empresaId.valor() : "null"));
    }

    public UsuarioNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
