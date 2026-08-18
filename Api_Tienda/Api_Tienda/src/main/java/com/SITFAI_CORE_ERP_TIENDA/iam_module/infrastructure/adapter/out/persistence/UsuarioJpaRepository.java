package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository para UsuarioJpaEntity.
 * Obligatoriamente filtra por empresaId (MT-01).
 */
@Repository
public interface UsuarioJpaRepository extends JpaRepository<UsuarioJpaEntity, String> {

    Optional<UsuarioJpaEntity> findByEmpresaIdAndId(String empresaId, String id);

    Optional<UsuarioJpaEntity> findByEmpresaIdAndUsername(String empresaId, String username);

    Optional<UsuarioJpaEntity> findByEmpresaIdAndEmail(String empresaId, String email);

    boolean existsByEmpresaIdAndUsername(String empresaId, String username);

    boolean existsByEmpresaIdAndEmail(String empresaId, String email);

    List<UsuarioJpaEntity> findByEmpresaId(String empresaId);
}
