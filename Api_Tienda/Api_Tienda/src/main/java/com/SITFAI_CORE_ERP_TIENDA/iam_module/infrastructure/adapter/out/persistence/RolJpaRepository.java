package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface RolJpaRepository extends JpaRepository<RolJpaEntity, String> {
    Optional<RolJpaEntity> findByCodigo(String codigo);
}
