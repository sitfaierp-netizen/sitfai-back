package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.PedidoId;

/**
 * ExcepciÃ³n lanzada cuando un Pedido no existe o no pertenece al Tenant (MT-01, MT-02).
 */
public class PedidoNoEncontradoException extends DomainException {

    public PedidoNoEncontradoException(PedidoId pedidoId, EmpresaId empresaId) {
        super("ERR_PEDIDO_NO_ENCONTRADO",
                String.format("Pedido '%s' no encontrado para la empresa '%s'.", pedidoId, empresaId));
    }
}

