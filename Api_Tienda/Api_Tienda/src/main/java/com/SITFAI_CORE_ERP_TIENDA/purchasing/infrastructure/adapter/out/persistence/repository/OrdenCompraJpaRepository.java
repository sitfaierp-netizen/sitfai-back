package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity.OrdenCompraJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository para la entidad {@link OrdenCompraJpaEntity}.
 * <p>
 * Regla MT-01: Métodos de consulta con partición estricta por {@code empresa_id}.
 */
@Repository
public interface OrdenCompraJpaRepository extends JpaRepository<OrdenCompraJpaEntity, String> {

    Optional<OrdenCompraJpaEntity> findByIdAndEmpresaId(String id, String empresaId);

    List<OrdenCompraJpaEntity> findByEmpresaId(String empresaId);

    List<OrdenCompraJpaEntity> findByEmpresaIdAndEstado(String empresaId, String estado);

    List<OrdenCompraJpaEntity> findByProveedorIdAndEmpresaId(String proveedorId, String empresaId);

    boolean existsByIdAndEmpresaId(String id, String empresaId);
}
