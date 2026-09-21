package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.adapter;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity.PedidoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.mapper.PedidoPersistenceMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.repository.PedidoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component("apiTiendaPedidoJpaAdapter")
public class PedidoJpaAdapter implements PedidoRepository {

    private final PedidoJpaRepository repository;

    public PedidoJpaAdapter(PedidoJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Pedido guardar(Pedido pedido) {
        PedidoJpaEntity entity = PedidoPersistenceMapper.toEntity(pedido);
        
        // Conservar version si ya existe
        Optional<PedidoJpaEntity> existente = repository.findByIdAndEmpresaId(entity.getId(), entity.getEmpresaId());
        existente.ifPresent(e -> {
            entity.setVersion(e.getVersion());
            entity.setCreadoPor(e.getCreadoPor());
            // Map original line versions if necessary for JPA updates
        });
        
        PedidoJpaEntity saved = repository.save(entity);
        return PedidoPersistenceMapper.toDomain(saved);
    }

    @Override
    public Optional<Pedido> buscarPorId(PedidoId id, EmpresaId empresaId) {
        return repository.findByIdAndEmpresaId(id.valor().toString(), empresaId.valor().toString())
                .map(PedidoPersistenceMapper::toDomain);
    }

    @Override
    public List<Pedido> buscarPorEmpresa(EmpresaId empresaId) {
        return repository.findByEmpresaId(empresaId.valor().toString()).stream()
                .map(PedidoPersistenceMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existePorId(PedidoId id, EmpresaId empresaId) {
        return repository.existsByIdAndEmpresaId(id.valor().toString(), empresaId.valor().toString());
    }
}
