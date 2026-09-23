package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EstadoTurno;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TurnoId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.port.output.TurnoCajaRepository;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.entity.TurnoCajaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.repository.TurnoCajaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Driven Adapter: Implementación del Output Port {@link TurnoCajaRepository} con Spring Data JPA.
 * <p>
 * Regla MT-01: Exige y valida el {@link EmpresaId} en todas las operaciones.
 */
@Component("posTurnoCajaJpaAdapter")
public class TurnoCajaJpaAdapter implements
        TurnoCajaRepository,
        com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TurnoCajaRepository {

    private final TurnoCajaJpaRepository repository;

    public TurnoCajaJpaAdapter(TurnoCajaJpaRepository repository) {
        this.repository = Objects.requireNonNull(repository, "TurnoCajaJpaRepository es obligatorio");
    }

    // ==========================================
    // Métodos del Puerto de Dominio Canónico
    // ==========================================

    @Override
    public TurnoCaja guardar(TurnoCaja turno, EmpresaId empresaId) {
        Objects.requireNonNull(turno, "El turno no puede ser nulo");
        Objects.requireNonNull(empresaId, "El empresaId es obligatorio (MT-01)");

        TurnoCajaJpaEntity entity = TurnoCajaPersistenceMapper.toJpaEntity(turno);
        TurnoCajaJpaEntity guardada = repository.save(entity);
        return TurnoCajaPersistenceMapper.toDomainEntity(guardada);
    }

    @Override
    public Optional<TurnoCaja> buscarPorId(TurnoId id, EmpresaId empresaId) {
        Objects.requireNonNull(id, "El TurnoId no puede ser nulo");
        Objects.requireNonNull(empresaId, "El EmpresaId es obligatorio (MT-01)");

        return repository.findByIdAndEmpresaId(id.value().toString(), empresaId.value().toString())
                .map(TurnoCajaPersistenceMapper::toDomainEntity);
    }

    @Override
    public Optional<TurnoCaja> buscarTurnoAbiertoPorCaja(CajaId cajaId, EmpresaId empresaId) {
        Objects.requireNonNull(cajaId, "El CajaId no puede ser nulo");
        Objects.requireNonNull(empresaId, "El EmpresaId es obligatorio (MT-01)");

        return repository.findByCajaIdAndEmpresaIdAndEstado(
                        cajaId.value().toString(),
                        empresaId.value().toString(),
                        EstadoTurno.ABIERTO.name())
                .map(TurnoCajaPersistenceMapper::toDomainEntity);
    }

    @Override
    public List<TurnoCaja> listarPorEmpresa(EmpresaId empresaId) {
        Objects.requireNonNull(empresaId, "El EmpresaId es obligatorio (MT-01)");

        return repository.findAllByEmpresaId(empresaId.value().toString()).stream()
                .map(TurnoCajaPersistenceMapper::toDomainEntity)
                .collect(Collectors.toList());
    }

    // ==========================================
    // Métodos de Compatibilidad con Puerto Legado
    // ==========================================

    @Override
    public com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja guardar(
            com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja turno) {
        Objects.requireNonNull(turno, "El turno legado no puede ser nulo");
        TurnoCajaJpaEntity entity = TurnoCajaPersistenceMapper.toJpaEntityLegacy(turno);
        TurnoCajaJpaEntity guardada = repository.save(entity);
        return TurnoCajaPersistenceMapper.toLegacyDomainEntity(guardada);
    }

    @Override
    public Optional<com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja> buscarPorId(
            com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.TurnoId id,
            com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId empresaId) {
        Objects.requireNonNull(id, "El TurnoId legado no puede ser nulo");
        Objects.requireNonNull(empresaId, "El EmpresaId legado no puede ser nulo");

        return repository.findByIdAndEmpresaId(id.value().toString(), empresaId.value().toString())
                .map(TurnoCajaPersistenceMapper::toLegacyDomainEntity);
    }
}
