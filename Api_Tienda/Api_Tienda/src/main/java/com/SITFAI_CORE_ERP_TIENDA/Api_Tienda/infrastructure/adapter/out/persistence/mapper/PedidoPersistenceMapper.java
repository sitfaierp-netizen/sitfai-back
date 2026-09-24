package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.mapper;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EstadoPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity.LineaPedidoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity.PedidoJpaEntity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PedidoPersistenceMapper {

    public static PedidoJpaEntity toEntity(Pedido domain) {
        PedidoJpaEntity entity = new PedidoJpaEntity();
        entity.setId(domain.getId().valor().toString());
        entity.setEmpresaId(domain.getEmpresaId().valor().toString());
        entity.setClienteId(domain.getClienteId().valor().toString());
        entity.setEstado(domain.getEstado().name());
        entity.setTotal(domain.calcularTotal().monto());
        entity.setMoneda(domain.calcularTotal().moneda());
        entity.setCreadoEn(domain.getCreadoEn());
        entity.setActualizadoEn(domain.getActualizadoEn());

        List<LineaPedidoJpaEntity> lineas = domain.getLineas().stream().map(l -> {
            LineaPedidoJpaEntity lineaEntity = new LineaPedidoJpaEntity();
            lineaEntity.setId(l.getId().toString());
            lineaEntity.setEmpresaId(domain.getEmpresaId().valor().toString());
            lineaEntity.setProductoId(l.getProductoId().valor().toString());
            lineaEntity.setCantidad(l.getCantidad());
            lineaEntity.setPrecioUnitario(l.getPrecioUnitario().monto());
            lineaEntity.setMoneda(l.getPrecioUnitario().moneda());
            lineaEntity.setSubtotal(l.subtotal().monto());
            lineaEntity.setPedido(entity);
            return lineaEntity;
        }).collect(Collectors.toList());

        entity.setLineas(lineas);
        return entity;
    }

    public static Pedido toDomain(PedidoJpaEntity entity) {
        List<LineaPedido> lineas = entity.getLineas() != null ? entity.getLineas().stream().map(l -> new LineaPedido(
                UUID.fromString(l.getId()),
                new ProductoId(UUID.fromString(l.getProductoId())),
                l.getCantidad(),
                Dinero.de(l.getPrecioUnitario())
        )).collect(Collectors.toList()) : List.of();

        return Pedido.reconstruir(
                new PedidoId(UUID.fromString(entity.getId())),
                new EmpresaId(UUID.fromString(entity.getEmpresaId())),
                new ClienteId(UUID.fromString(entity.getClienteId())),
                EstadoPedido.valueOf(entity.getEstado()),
                lineas,
                entity.getCreadoEn(),
                entity.getActualizadoEn()
        );
    }
}
