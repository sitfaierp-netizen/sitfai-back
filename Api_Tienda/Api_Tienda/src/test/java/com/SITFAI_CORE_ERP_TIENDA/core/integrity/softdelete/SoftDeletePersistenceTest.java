package com.SITFAI_CORE_ERP_TIENDA.core.integrity.softdelete;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.EmpresaJpaEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class SoftDeletePersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void debeAplicarSoftDeleteMedianteSQLDelete() {
        EmpresaJpaEntity empresa = new EmpresaJpaEntity();
        empresa.setId(UUID.randomUUID().toString());
        empresa.setRuc("99988877766");
        empresa.setRazonSocial("Empresa a Borrar");
        empresa.setEstado("ACTIVA");
        empresa.setCreadoEn(Instant.now());
        empresa.setActualizadoEn(Instant.now());
        empresa.setActivo(true);
        empresa.setVersion(0L);

        entityManager.persist(empresa);
        entityManager.flush();

        String id = empresa.getId();
        
        // Remove invokes @SQLDelete
        entityManager.remove(empresa);
        entityManager.flush();
        entityManager.clear();

        // El registro ya no debe existir segun JPA (@SQLRestriction)
        EmpresaJpaEntity deletedEmpresa = entityManager.find(EmpresaJpaEntity.class, id);
        assertNull(deletedEmpresa, "La empresa deberia estar filtrada por @SQLRestriction");
    }
}
