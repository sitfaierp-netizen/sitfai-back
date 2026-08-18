package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;

/**
 * Excepción de Dominio: Lanzada cuando no se encuentra una Bodega
 * requerida para una operación, validando implícitamente el tenant (MT-01).
 */
public class BodegaNoEncontradaException extends DomainException {
    
    private final BodegaId bodegaId;
    private final EmpresaId empresaId;
    
    public BodegaNoEncontradaException(BodegaId bodegaId, EmpresaId empresaId) {
        super("BOD-404", String.format("Bodega '%s' no encontrada para el tenant '%s'.", bodegaId, empresaId));
        this.bodegaId = bodegaId;
        this.empresaId = empresaId;
    }

    public BodegaId getBodegaId() { return bodegaId; }
    public EmpresaId getEmpresaId() { return empresaId; }
}
