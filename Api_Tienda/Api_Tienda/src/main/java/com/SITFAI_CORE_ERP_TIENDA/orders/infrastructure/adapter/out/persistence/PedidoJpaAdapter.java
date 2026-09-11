package com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence.entity.OrdersPedidoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence.mapper.PedidoPersistenceMapper;
import com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence.repository.OrdersPedidoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component("ordersPedidoJpaAdapter")
public class PedidoJpaAdapter implements PedidoRepository {

    private final OrdersPedidoJpaRepository repository;

    public PedidoJpaAdapter(OrdersPedidoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Pedido pedido) {
        OrdersPedidoJpaEntity entity = PedidoPersistenceMapper.toEntity(pedido);
        repository.save(entity);
    }

    @Override
    public Optional<Pedido> findByIdAndEmpresaId(PedidoId id, UUID empresaId) {
        return repository.findByIdAndEmpresaId(id.value(), empresaId)
                .map(PedidoPersistenceMapper::toDomain);
    }
}
