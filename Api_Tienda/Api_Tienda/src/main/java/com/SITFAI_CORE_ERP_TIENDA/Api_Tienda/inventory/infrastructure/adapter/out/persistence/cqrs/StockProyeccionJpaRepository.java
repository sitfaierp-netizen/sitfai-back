package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.cqrs;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Spring Data Repository dedicado EXCLUSIVAMENTE al modelo de lectura.
 */
@Repository
public interface StockProyeccionJpaRepository extends JpaRepository<StockProyeccionJpaEntity, StockProyeccionId> {
    
    // Consulta optimizada para la vista desnormalizada (requiere empresaId por MT-01)
    List<StockProyeccionJpaEntity> findByEmpresaIdAndBodegaId(UUID empresaId, UUID bodegaId);
    
}
