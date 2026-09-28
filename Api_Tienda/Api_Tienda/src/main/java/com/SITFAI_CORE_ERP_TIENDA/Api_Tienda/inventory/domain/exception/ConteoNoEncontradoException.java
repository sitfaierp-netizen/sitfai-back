package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.ConteoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.EmpresaId;

/**
 * Excepción de Dominio: Lanzada cuando no se encuentra un Conteo Cíclico
 * para el tenant indicado, validando implícitamente el aislamiento multi-tenant (MT-01).
 */
public class ConteoNoEncontradoException extends DomainException {

    private final ConteoId conteoId;
    private final EmpresaId empresaId;

    public ConteoNoEncontradoException(ConteoId conteoId, EmpresaId empresaId) {
        super("CONTEO-404", String.format("Conteo Cíclico '%s' no encontrado para el tenant '%s'.", conteoId, empresaId));
        this.conteoId = conteoId;
        this.empresaId = empresaId;
    }

    public ConteoId getConteoId() {
        return conteoId;
    }

    public EmpresaId getEmpresaId() {
        return empresaId;
    }
}
