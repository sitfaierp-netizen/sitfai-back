package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.adapter;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity.PedidoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.repository.PedidoJpaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.mapper.PedidoPersistenceMapper;
import org.springframework.stereotype.Repository;

import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador de Persistencia JPA: Implementación del puerto de salida {@link PedidoRepository}.
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura (REGLA-1, REGLA-6).
 * Conecta el modelo del dominio con la base de datos relacional MySQL a través de Spring Data JPA.
 */
@Repository
public class PedidoJpaAdapter implements PedidoRepository {

    private final PedidoJpaRepository jpaRepository;
    private final PedidoPersistenceMapper persistenceMapper;

    public PedidoJpaAdapter(
            PedidoJpaRepository jpaRepository,
            PedidoPersistenceMapper persistenceMapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository es requerido.");
        this.persistenceMapper = Objects.requireNonNull(persistenceMapper, "persistenceMapper es requerido.");
    }

    @Override
    public Pedido guardar(Pedido pedido) {
        Objects.requireNonNull(pedido, "PedidoJpaAdapter: pedido no puede ser null.");

        PedidoJpaEntity entity = persistenceMapper.toEntity(pedido);
        PedidoJpaEntity savedEntity = jpaRepository.save(entity);

        return persistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Pedido> buscarPorId(PedidoId id, EmpresaId empresaId) {
        Objects.requireNonNull(id, "PedidoJpaAdapter: id es requerido.");
        Objects.requireNonNull(empresaId, "PedidoJpaAdapter: empresaId es requerido (MT-01).");

        return jpaRepository.findByIdAndEmpresaId(id.valor(), empresaId.valor())
                .map(persistenceMapper::toDomain);
    }

    @Override
    public java.util.List<Pedido> buscarPorEmpresa(EmpresaId empresaId) {
        Objects.requireNonNull(empresaId, "PedidoJpaAdapter: empresaId es requerido (MT-01).");

        return jpaRepository.findByEmpresaId(empresaId.valor()).stream()
                .map(persistenceMapper::toDomain)
                .toList();
    }

    @Override
    public boolean existePorId(PedidoId id, EmpresaId empresaId) {
        Objects.requireNonNull(id, "PedidoJpaAdapter: id es requerido.");
        Objects.requireNonNull(empresaId, "PedidoJpaAdapter: empresaId es requerido (MT-01).");

        return jpaRepository.existsByIdAndEmpresaId(id.valor(), empresaId.valor());
    }
}
