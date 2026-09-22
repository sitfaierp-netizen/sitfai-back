package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.EmitirOrdenCompraCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;

/**
 * Driving / Input Port: Caso de Uso para Emitir una Orden de Compra formal al proveedor.
 */
public interface EmitirOrdenCompraUseCase {

    /**
     * Emite formalmente la Orden de Compra garantizando aislamiento MT-01,
     * persistencia atómica y publicación asíncrona del evento de dominio.
     *
     * @param command Comando con los datos requeridos para la emisión.
     * @return Representación DTO de la orden emitida.
     */
    OrdenCompraResponse emitirOrdenCompra(EmitirOrdenCompraCommand command);
}
