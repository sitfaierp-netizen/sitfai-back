package com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence.mapper;

import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence.entity.OrdersLineaPedidoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence.entity.OrdersPedidoJpaEntity;

import java.util.List;

public class PedidoPersistenceMapper {

    public static OrdersPedidoJpaEntity toEntity(Pedido domain) {
        if (domain == null) return null;

        OrdersPedidoJpaEntity entity = new OrdersPedidoJpaEntity();
        entity.setId(domain.getId().value());
        entity.setEmpresaId(domain.getEmpresaId());
        entity.setClienteId(domain.getClienteId().value());
        entity.setTotalMonetario(domain.getTotalMonetario().monto());
        entity.setEstado(domain.getEstado());
        entity.setVersion(domain.getVersion());
        
        entity.setCreadoEn(domain.getCreatedAt());
        entity.setCreadoPor(domain.getCreatedBy());
        entity.setActualizadoEn(domain.getUpdatedAt());
        entity.setActualizadoPor(domain.getUpdatedBy());

        if (domain.getLineas() != null) {
            domain.getLineas().forEach(lineaDomain -> {
                OrdersLineaPedidoJpaEntity lineaEntity = new OrdersLineaPedidoJpaEntity();
                lineaEntity.setId(lineaDomain.getId());
                lineaEntity.setProductoId(lineaDomain.getProductoId().value());
                lineaEntity.setCantidad(lineaDomain.getCantidad());
                lineaEntity.setPrecioUnitario(lineaDomain.getPrecioUnitario().monto());
                lineaEntity.setPedido(entity); // Asignación bidireccional
                entity.getLineas().add(lineaEntity);
            });
        }

        return entity;
    }

    public static Pedido toDomain(OrdersPedidoJpaEntity entity) {
        if (entity == null) return null;

        List<LineaPedido> lineasDomain = entity.getLineas().stream()
                .map(PedidoPersistenceMapper::toLineaDomain)
                .toList();

        return new Pedido(
                new PedidoId(entity.getId()),
                entity.getEmpresaId(),
                new ClienteId(entity.getClienteId()),
                lineasDomain,
                new Dinero(entity.getTotalMonetario()),
                entity.getEstado(),
                entity.getVersion(),
                entity.getCreadoEn(),
                entity.getCreadoPor(),
                entity.getActualizadoEn(),
                entity.getActualizadoPor()
        );
    }

    private static LineaPedido toLineaDomain(OrdersLineaPedidoJpaEntity entity) {
        return new LineaPedido(
                entity.getId(),
                new ProductoId(entity.getProductoId()),
                entity.getCantidad(),
                new Dinero(entity.getPrecioUnitario())
        );
    }
}
