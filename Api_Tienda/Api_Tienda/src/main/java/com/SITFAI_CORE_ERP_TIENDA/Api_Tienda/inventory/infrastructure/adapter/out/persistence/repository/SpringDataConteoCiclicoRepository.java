package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.EstadoConteo;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.ConteoCiclicoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository para {@link ConteoCiclicoJpaEntity}.
 * <p>
 * Regla MT-01: Métodos acotados obligatoriamente por {@code empresaId}.
 */
@Repository
public interface SpringDataConteoCiclicoRepository extends JpaRepository<ConteoCiclicoJpaEntity, String> {

    Optional<ConteoCiclicoJpaEntity> findByIdAndEmpresaId(String id, String empresaId);

    List<ConteoCiclicoJpaEntity> findByEmpresaIdAndBodegaId(String empresaId, String bodegaId);

    List<ConteoCiclicoJpaEntity> findByEmpresaIdAndEstado(String empresaId, EstadoConteo estado);

    boolean existsByIdAndEmpresaId(String id, String empresaId);
}
