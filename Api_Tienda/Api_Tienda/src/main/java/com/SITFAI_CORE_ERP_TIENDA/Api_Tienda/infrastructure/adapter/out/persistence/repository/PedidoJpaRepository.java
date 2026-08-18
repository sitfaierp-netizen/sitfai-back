package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity.PedidoJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository para {@link PedidoJpaEntity}.
 * <p>
 * Pertenece exclusivamente a la infraestructura de persistencia (REGLA-6).
 * Incluye métodos de búsqueda filtrados por {@code empresaId} para cumplir con MT-01 y MT-02.
 */
@Repository
public interface PedidoJpaRepository extends JpaRepository<PedidoJpaEntity, UUID> {

    Optional<PedidoJpaEntity> findByIdAndEmpresaId(UUID id, UUID empresaId);

    List<PedidoJpaEntity> findByEmpresaId(UUID empresaId);

    boolean existsByIdAndEmpresaId(UUID id, UUID empresaId);
}
