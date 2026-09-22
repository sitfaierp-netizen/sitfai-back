package com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.StoredDomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EventStatus;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.StoredEventId;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.port.output.EventStoreRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.adapter.out.persistence.entity.StoredEventJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.adapter.out.persistence.mapper.EventStoreMapper;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.adapter.out.persistence.repository.StoredEventJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Driven Adapter: Implementación JPA del puerto de salida {@link EventStoreRepository} (AUD-03, MT-01).
 */
@Repository
@Transactional(readOnly = true)
public class EventStoreJpaAdapter implements EventStoreRepository, com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.port.EventStoreRepository {

    private final StoredEventJpaRepository jpaRepository;
    private final EventStoreMapper mapper;

    public EventStoreJpaAdapter(StoredEventJpaRepository jpaRepository, EventStoreMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository, "jpaRepository no puede ser null");
        this.mapper = Objects.requireNonNull(mapper, "mapper no puede ser null");
    }

    @Override
    @Transactional
    public StoredDomainEvent guardar(StoredDomainEvent evento) {
        Objects.requireNonNull(evento, "evento no puede ser null");
        StoredEventJpaEntity entity = mapper.toEntity(evento);
        StoredEventJpaEntity guardada = jpaRepository.save(entity);
        return mapper.toDomain(guardada);
    }

    @Override
    public Optional<StoredDomainEvent> buscarPorId(StoredEventId id, EmpresaId empresaId) {
        Objects.requireNonNull(id, "id no puede ser null");
        Objects.requireNonNull(empresaId, "empresaId no puede ser null (MT-01)");
        return jpaRepository.findByIdAndEmpresaId(id.valor(), empresaId.valor())
                .map(mapper::toDomain);
    }

    @Override
    public List<StoredDomainEvent> buscarPorEmpresa(EmpresaId empresaId) {
        Objects.requireNonNull(empresaId, "empresaId no puede ser null (MT-01)");
        return jpaRepository.findByEmpresaIdOrderByOcurridoEnDesc(empresaId.valor())
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<StoredDomainEvent> buscarPorEmpresaYEstado(EmpresaId empresaId, EventStatus estado) {
        Objects.requireNonNull(empresaId, "empresaId no puede ser null (MT-01)");
        Objects.requireNonNull(estado, "estado no puede ser null");
        return jpaRepository.findByEmpresaIdAndEstadoOrderByOcurridoEnAsc(empresaId.valor(), estado)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<StoredDomainEvent> buscarPorEmpresaYNombreEvento(EmpresaId empresaId, String nombreEvento) {
        Objects.requireNonNull(empresaId, "empresaId no puede ser null (MT-01)");
        Objects.requireNonNull(nombreEvento, "nombreEvento no puede ser null");
        return jpaRepository.findByEmpresaIdAndNombreEventoOrderByOcurridoEnDesc(empresaId.valor(), nombreEvento)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public List<StoredDomainEvent> buscarPendientes(int limite) {
        int tamanoLote = limite > 0 ? limite : 50;
        return jpaRepository.findByEstadoOrderByOcurridoEnAsc(
                        EventStatus.PENDIENTE,
                        org.springframework.data.domain.PageRequest.of(0, tamanoLote)
                )
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}
