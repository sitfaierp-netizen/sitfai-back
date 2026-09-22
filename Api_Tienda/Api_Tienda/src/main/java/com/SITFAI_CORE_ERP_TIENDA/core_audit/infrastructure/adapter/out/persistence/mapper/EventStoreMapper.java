package com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.adapter.out.persistence.mapper;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.StoredDomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.StoredEventId;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.adapter.out.persistence.entity.StoredEventJpaEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper bidireccional entre el Aggregate Root de Dominio {@link StoredDomainEvent}
 * y la entidad JPA de persistencia {@link StoredEventJpaEntity}.
 */
@Component
public class EventStoreMapper {

    public StoredEventJpaEntity toEntity(StoredDomainEvent domain) {
        if (domain == null) {
            return null;
        }

        return new StoredEventJpaEntity(
                domain.getId().valor(),
                domain.getEmpresaId().valor(),
                domain.getNombreEvento(),
                domain.getOcurridoEn(),
                domain.getPayload(),
                domain.getEstado(),
                domain.getMotivoFallo(),
                domain.getProcesadoEn()
        );
    }

    public StoredDomainEvent toDomain(StoredEventJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return StoredDomainEvent.reconstituir(
                StoredEventId.de(entity.getId()),
                EmpresaId.de(entity.getEmpresaId()),
                entity.getNombreEvento(),
                entity.getOcurridoEn(),
                entity.getPayload(),
                entity.getEstado(),
                entity.getMotivoFallo(),
                entity.getProcesadoEn()
        );
    }
}
