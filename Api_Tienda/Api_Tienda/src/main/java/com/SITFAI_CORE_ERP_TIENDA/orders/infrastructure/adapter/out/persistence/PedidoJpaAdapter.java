package com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence.entity.PedidoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence.mapper.PedidoPersistenceMapper;
import com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence.repository.PedidoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class PedidoJpaAdapter implements PedidoRepository {

    private final PedidoJpaRepository repository;

    public PedidoJpaAdapter(PedidoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Pedido pedido) {
        PedidoJpaEntity entity = PedidoPersistenceMapper.toEntity(pedido);
        if (!repository.existsById(entity.getId())) {
            entity.setVersion(null);
        }
        repository.saveAndFlush(entity);
    }

    @Override
    public Optional<Pedido> findByIdAndEmpresaId(PedidoId id, UUID empresaId) {
        return repository.findByIdAndEmpresaId(id.value(), empresaId)
                .map(PedidoPersistenceMapper::toDomain);
    }
}
