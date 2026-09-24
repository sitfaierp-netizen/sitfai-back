package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity.SolicitudJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Spring Data JPA Repository para SolicitudJpaEntity.
 * El Dominio NO conoce este repositorio — solo lo usa el Adapter.
 * Regla MT-01: Toda consulta filtra obligatoriamente por empresa_id.
 */
public interface SolicitudJpaRepository extends JpaRepository<SolicitudJpaEntity, String> {

    /** MT-01: búsqueda siempre filtrada por tenant. */
    Optional<SolicitudJpaEntity> findByIdAndEmpresaId(String id, String empresaId);

    /**
     * Busca la solicitud más reciente para una empresa + bodega + producto.
     * Usado por el test de integración para verificar la creación asíncrona.
     * Regla MT-01: filtrado estricto por empresa_id.
     */
    @Query("SELECT s FROM SolicitudJpaEntity s JOIN s.lineas l " +
           "WHERE s.empresaId = :empresaId " +
           "AND s.bodegaId = :bodegaId " +
           "AND l.productoId = :productoId " +
           "ORDER BY s.creadoEn DESC")
    Optional<SolicitudJpaEntity> findMasRecienteByEmpresaIdAndBodegaIdAndProductoId(
            @Param("empresaId") String empresaId,
            @Param("bodegaId") String bodegaId,
            @Param("productoId") String productoId
    );
}
