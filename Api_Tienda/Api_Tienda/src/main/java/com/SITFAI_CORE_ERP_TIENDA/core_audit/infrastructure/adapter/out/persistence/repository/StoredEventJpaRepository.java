package com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EventStatus;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.infrastructure.adapter.out.persistence.entity.StoredEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository para {@link StoredEventJpaEntity}.
 * <p>
 * Regla MT-01: Métodos de consulta filtrados por {@code empresaId}.
 */
public interface StoredEventJpaRepository extends JpaRepository<StoredEventJpaEntity, UUID> {

    Optional<StoredEventJpaEntity> findByIdAndEmpresaId(UUID id, UUID empresaId);

    List<StoredEventJpaEntity> findByEmpresaIdOrderByOcurridoEnDesc(UUID empresaId);

    List<StoredEventJpaEntity> findByEmpresaIdAndEstadoOrderByOcurridoEnAsc(UUID empresaId, EventStatus estado);

    List<StoredEventJpaEntity> findByEmpresaIdAndNombreEventoOrderByOcurridoEnDesc(UUID empresaId, String nombreEvento);

    List<StoredEventJpaEntity> findByEstadoOrderByOcurridoEnAsc(EventStatus estado, org.springframework.data.domain.Pageable pageable);
}
