package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.mapper;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.EstadoPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity.LineaPedidoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity.PedidoJpaEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper de Persistencia: Conversión bidireccional entre el Agregado de Dominio {@link Pedido}
 * y el modelo de persistencia JPA {@link PedidoJpaEntity} (REGLA-1, REGLA-6).
 */
@Component
public class PedidoPersistenceMapper {

    /**
     * Convierte una entidad JPA {@link PedidoJpaEntity} al Agregado {@link Pedido} del Dominio.
     */
    public Pedido toDomain(PedidoJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        List<LineaPedido> lineasDominio = new ArrayList<>();
        if (entity.getLineas() != null) {
            lineasDominio = entity.getLineas().stream()
                    .map(this::toDomainLinea)
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        return Pedido.reconstruir(
                PedidoId.de(entity.getId()),
                EmpresaId.de(entity.getEmpresaId()),
                ClienteId.de(entity.getClienteId()),
                EstadoPedido.valueOf(entity.getEstado()),
                lineasDominio,
                entity.getCreadoEn(),
                entity.getActualizadoEn()
        );
    }

    /**
     * Convierte el Agregado {@link Pedido} del Dominio a la entidad JPA {@link PedidoJpaEntity}.
     */
    public PedidoJpaEntity toEntity(Pedido domain) {
        if (domain == null) {
            return null;
        }

        Dinero total = domain.calcularTotal();

        PedidoJpaEntity entity = new PedidoJpaEntity(
                domain.getId().valor(),
                domain.getEmpresaId().valor(),
                domain.getClienteId().valor(),
                domain.getEstado().name(),
                total.monto(),
                total.moneda(),
                domain.getCreadoEn(),
                domain.getActualizadoEn()
        );

        if (domain.getLineas() != null) {
            for (LineaPedido linea : domain.getLineas()) {
                LineaPedidoJpaEntity lineaEntity = new LineaPedidoJpaEntity(
                        linea.getId(),
                        entity,
                        domain.getEmpresaId().valor(),
                        linea.getProductoId().valor(),
                        linea.getCantidad(),
                        linea.getPrecioUnitario().monto(),
                        linea.getPrecioUnitario().moneda(),
                        linea.subtotal().monto()
                );
                entity.agregarLinea(lineaEntity);
            }
        }

        return entity;
    }

    private LineaPedido toDomainLinea(LineaPedidoJpaEntity lineaEntity) {
        return new LineaPedido(
                lineaEntity.getId(),
                ProductoId.de(lineaEntity.getProductoId()),
                lineaEntity.getCantidad(),
                Dinero.de(lineaEntity.getPrecioUnitario(), lineaEntity.getMoneda())
        );
    }
}
