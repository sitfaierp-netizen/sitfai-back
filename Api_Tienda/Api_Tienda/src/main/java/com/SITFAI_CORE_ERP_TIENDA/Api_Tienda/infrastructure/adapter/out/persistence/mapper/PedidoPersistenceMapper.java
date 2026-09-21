package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.mapper;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.EstadoPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ProductoId;
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
        List<LineaPedido> lineas = entity.getLineas().stream().map(l -> new LineaPedido(
                UUID.fromString(l.getId()),
                ProductoId.de(UUID.fromString(l.getProductoId())),
                Cantidad.de(l.getCantidad()),
                Dinero.de(l.getPrecioUnitario(), l.getMoneda())
        )).collect(Collectors.toList());

        return Pedido.reconstruir(
                PedidoId.de(UUID.fromString(entity.getId())),
                EmpresaId.de(UUID.fromString(entity.getEmpresaId())),
                ClienteId.de(UUID.fromString(entity.getClienteId())),
                EstadoPedido.valueOf(entity.getEstado()),
                lineas,
                entity.getCreadoEn(),
                entity.getActualizadoEn()
        );
    }
}
