package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TurnoCajaRepository;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.TurnoId;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.entity.TurnoCajaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.repository.TurnoCajaJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TurnoCajaJpaAdapter implements TurnoCajaRepository {

    private final TurnoCajaJpaRepository repository;

    public TurnoCajaJpaAdapter(TurnoCajaJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public TurnoCaja guardar(TurnoCaja turno) {
        TurnoCajaJpaEntity entity = TurnoCajaPersistenceMapper.toJpaEntity(turno);
        TurnoCajaJpaEntity guardada = repository.save(entity);
        return TurnoCajaPersistenceMapper.toDomainEntity(guardada);
    }

    @Override
    public Optional<TurnoCaja> buscarPorId(TurnoId id, EmpresaId empresaId) {
        return repository.findByIdAndEmpresaId(id.value().toString(), empresaId.value().toString())
                .map(TurnoCajaPersistenceMapper::toDomainEntity);
    }
}
