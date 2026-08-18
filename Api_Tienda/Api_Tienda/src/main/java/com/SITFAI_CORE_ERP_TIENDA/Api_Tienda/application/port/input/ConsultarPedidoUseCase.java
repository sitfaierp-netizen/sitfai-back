package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;

import java.util.List;
import java.util.UUID;

/**
 * Driving Port (API): Caso de uso para consultar pedidos por ID o por Empresa.
 */
public interface ConsultarPedidoUseCase {

    PedidoResponse porId(UUID pedidoId, UUID empresaId);

    List<PedidoResponse> porEmpresa(UUID empresaId);
}
