package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.adapter;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.Despacho;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.DespachoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.DespachoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.mapper.DespachoPersistenceMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.repository.DespachoJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Adaptador de Salida (Driven Adapter): Implementación JPA del puerto {@link DespachoRepository}.
 * <p>
 * Regla 1: Pertenece exclusivamente a la Capa de Infraestructura.
 * Regla MT-01: Exige y valida el {@code empresaId} en todas las operaciones.
 */
@Component
public class DespachoJpaAdapter implements DespachoRepository {

    private final DespachoJpaRepository jpaRepository;
    private final DespachoPersistenceMapper mapper;

    public DespachoJpaAdapter(DespachoJpaRepository jpaRepository, DespachoPersistenceMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "DespachoJpaRepository es obligatorio");
        this.mapper = Objects.requireNonNull(mapper, "DespachoPersistenceMapper es obligatorio");
    }

    @Override
    public Despacho guardar(Despacho despacho, EmpresaId empresaId) {
        Objects.requireNonNull(despacho, "El despacho a guardar no puede ser nulo");
        Objects.requireNonNull(empresaId, "El empresaId no puede ser nulo (MT-01)");

        DespachoJpaEntity entity = mapper.toEntity(despacho);
        DespachoJpaEntity guardada = jpaRepository.save(entity);
        return mapper.toDomain(guardada);
    }

    @Override
    public Optional<Despacho> buscarPorId(DespachoId id, EmpresaId empresaId) {
        Objects.requireNonNull(id, "El DespachoId no puede ser nulo");
        Objects.requireNonNull(empresaId, "El empresaId no puede ser nulo (MT-01)");

        return jpaRepository.findByIdAndEmpresaId(id.valor(), empresaId.valor())
                .map(mapper::toDomain);
    }

    @Override
    public Optional<Despacho> buscarPorPedidoId(PedidoId pedidoId, EmpresaId empresaId) {
        Objects.requireNonNull(pedidoId, "El PedidoId no puede ser nulo");
        Objects.requireNonNull(empresaId, "El empresaId no puede ser nulo (MT-01)");

        return jpaRepository.findByPedidoIdAndEmpresaId(pedidoId.valor(), empresaId.valor())
                .map(mapper::toDomain);
    }

    @Override
    public List<Despacho> listarPorEmpresa(EmpresaId empresaId) {
        Objects.requireNonNull(empresaId, "El empresaId no puede ser nulo (MT-01)");

        return jpaRepository.findAllByEmpresaId(empresaId.valor()).stream()
                .map(mapper::toDomain)
                .toList();
    }
}
