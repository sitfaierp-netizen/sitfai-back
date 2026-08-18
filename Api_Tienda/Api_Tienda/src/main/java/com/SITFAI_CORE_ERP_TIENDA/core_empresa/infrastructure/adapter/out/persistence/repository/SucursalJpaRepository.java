package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.repository;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.SucursalJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio Spring Data JPA para la entidad {@link SucursalJpaEntity}.
 * Filtra siempre por empresa_id para garantizar aislamiento multitenant (Regla MT-02).
 */
@Repository
public interface SucursalJpaRepository extends JpaRepository<SucursalJpaEntity, String> {

    /**
     * Retorna todas las Sucursales de una Empresa (MT-02 / SUC-01).
     */
    List<SucursalJpaEntity> findAllByEmpresa_Id(String empresaId);

    /**
     * Busca una Sucursal por su ID validando pertenencia a la Empresa (MT-02).
     */
    Optional<SucursalJpaEntity> findByIdAndEmpresa_Id(String sucursalId, String empresaId);
}
