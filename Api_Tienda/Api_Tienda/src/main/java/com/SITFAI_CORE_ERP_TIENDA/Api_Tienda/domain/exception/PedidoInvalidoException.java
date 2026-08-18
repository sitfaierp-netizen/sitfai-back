package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception;

/**
 * Excepción de Dominio lanzada cuando se violan las invariantes de un Pedido.
 */
public class PedidoInvalidoException extends DomainException {

    public PedidoInvalidoException(String mensaje) {
        super("ERR_PEDIDO_INVALIDO", mensaje);
    }
}
