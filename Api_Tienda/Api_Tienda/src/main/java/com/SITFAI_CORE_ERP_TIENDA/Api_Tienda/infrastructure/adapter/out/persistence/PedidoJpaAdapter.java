package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.port.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EstadoPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity.LineaPedidoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity.PedidoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.repository.PedidoJpaRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Driven Adapter: Implementa {@link PedidoRepository} para el nuevo Agregado {@link Pedido}.
 * <p>
 * Regla REGLA-6: Adaptador desacoplado del dominio puro.
 * Regla MT-01: Exige empresaId en todas las firmas para garantizar aislamiento por tenant.
 */
@Repository("pedidoModelJpaAdapter")
@Primary
public class PedidoJpaAdapter implements PedidoRepository {

    private final PedidoJpaRepository jpaRepository;

    public PedidoJpaAdapter(PedidoJpaRepository jpaRepository) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "PedidoJpaRepository no puede ser nulo");
    }

    @Override
    public void guardar(Pedido pedido) {
        Objects.requireNonNull(pedido, "pedido no puede ser nulo");

        String idStr = pedido.getId().valor().toString();
        String empresaIdStr = pedido.getEmpresaId().valor().toString();
        String clienteIdStr = pedido.getClienteId().valor().toString();
        String estadoStr = pedido.getEstado().name();

        PedidoJpaEntity entity = jpaRepository.findByIdAndEmpresaId(idStr, empresaIdStr)
                .orElseGet(() -> {
                    PedidoJpaEntity nueva = new PedidoJpaEntity();
                    nueva.setId(idStr);
                    nueva.setEmpresaId(empresaIdStr);
                    nueva.setClienteId(clienteIdStr);
                    nueva.setMoneda("USD");
                    return nueva;
                });

        entity.setEstado(estadoStr);
        entity.setTotal(pedido.calcularTotal().monto());

        // Actualizar o reemplazar líneas de detalle
        if (entity.getLineas() == null) {
            entity.setLineas(new ArrayList<>());
        } else {
            entity.getLineas().clear();
        }

        for (LineaPedido lp : pedido.getLineas()) {
            LineaPedidoJpaEntity lEntity = new LineaPedidoJpaEntity();
            lEntity.setId(UUID.randomUUID().toString());
            lEntity.setPedido(entity);
            lEntity.setEmpresaId(empresaIdStr);
            lEntity.setProductoId(lp.getProductoId().valor().toString());
            lEntity.setCantidad(lp.getCantidad());
            lEntity.setPrecioUnitario(lp.getPrecioUnitario().monto());
            lEntity.setMoneda("USD");
            lEntity.setSubtotal(lp.calcularSubtotal().monto());
            entity.getLineas().add(lEntity);
        }

        jpaRepository.save(entity);
    }

    @Override
    public Optional<Pedido> buscarPorId(EmpresaId empresaId, PedidoId id) {
        Objects.requireNonNull(empresaId, "empresaId no puede ser nulo");
        Objects.requireNonNull(id, "id no puede ser nulo");

        return jpaRepository.findByIdAndEmpresaId(id.valor().toString(), empresaId.valor().toString())
                .map(this::toDomain);
    }

    @Override
    public List<Pedido> listarPorEmpresa(EmpresaId empresaId) {
        Objects.requireNonNull(empresaId, "empresaId no puede ser nulo");

        return jpaRepository.findByEmpresaId(empresaId.valor().toString()).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existePorId(EmpresaId empresaId, PedidoId id) {
        Objects.requireNonNull(empresaId, "empresaId no puede ser nulo");
        Objects.requireNonNull(id, "id no puede ser nulo");

        return jpaRepository.existsByIdAndEmpresaId(id.valor().toString(), empresaId.valor().toString());
    }

    private Pedido toDomain(PedidoJpaEntity entity) {
        List<LineaPedido> lineas = entity.getLineas() != null
                ? entity.getLineas().stream()
                .map(l -> new LineaPedido(
                        new ProductoId(UUID.fromString(l.getProductoId())),
                        l.getCantidad(),
                        Dinero.de(l.getPrecioUnitario())
                ))
                .toList()
                : List.of();

        return Pedido.reconstituir(
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
