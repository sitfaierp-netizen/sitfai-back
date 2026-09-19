package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.Recepcion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.RecepcionId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.RecepcionRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.RecepcionJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.mapper.RecepcionPersistenceMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.repository.RecepcionJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RecepcionJpaAdapter implements RecepcionRepository {

    private final RecepcionJpaRepository jpaRepository;
    private final RecepcionPersistenceMapper mapper;

    public RecepcionJpaAdapter(RecepcionJpaRepository jpaRepository, RecepcionPersistenceMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public void guardar(Recepcion recepcion) {
        RecepcionJpaEntity entity = mapper.toEntity(recepcion);
        jpaRepository.save(entity);
    }

    @Override
    public Optional<Recepcion> buscarPorIdYEmpresaId(RecepcionId id, EmpresaId empresaId) {
        return jpaRepository.findByIdAndEmpresaId(id.valor(), empresaId.valor())
                .map(mapper::toDomain);
    }
}
