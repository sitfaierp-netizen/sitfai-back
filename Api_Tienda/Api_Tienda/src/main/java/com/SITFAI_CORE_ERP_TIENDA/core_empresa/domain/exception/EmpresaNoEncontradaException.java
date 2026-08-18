package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.Ruc;

/**
 * Excepción lanzada cuando una Empresa no es encontrada por su identificador o RUC.
 * (Mapea a HTTP 404 Not Found en Infraestructura).
 */
public class EmpresaNoEncontradaException extends DomainException {

    public EmpresaNoEncontradaException(EmpresaId empresaId) {
        super("Empresa no encontrada con ID: " + (empresaId != null ? empresaId.valor() : "null"));
    }

    public EmpresaNoEncontradaException(Ruc ruc) {
        super("No se encontró ninguna empresa con el RUC: " + (ruc != null ? ruc.valor() : "null"));
    }

    public EmpresaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
