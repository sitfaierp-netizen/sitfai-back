package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity.SolicitudJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Spring Data JPA Repository para SolicitudJpaEntity.
 * El Dominio NO conoce este repositorio — solo lo usa el Adapter.
 */
public interface SolicitudJpaRepository extends JpaRepository<SolicitudJpaEntity, String> {

    /** MT-01: búsqueda siempre filtrada por tenant. */
    Optional<SolicitudJpaEntity> findByIdAndEmpresaId(String id, String empresaId);
}
