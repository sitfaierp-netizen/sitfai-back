package com.SITFAI_CORE_ERP_TIENDA.orders.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.PedidoId;

import java.util.Optional;
import java.util.UUID;

public interface PedidoRepository {
    
    void save(Pedido pedido);
    
    Optional<Pedido> findByIdAndEmpresaId(PedidoId id, UUID empresaId);
    
    // Aquí se pueden añadir métodos adicionales como buscar por cliente
}
