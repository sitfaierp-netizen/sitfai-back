package com.SITFAI_CORE_ERP_TIENDA.core.integrity.softdelete;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.EmpresaJpaEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication.class)
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration.class)
class SoftDeleteQueryTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void lasConsultasJpqlNoDeberianRetornarRegistrosBorrados() {
        EmpresaJpaEntity activa = new EmpresaJpaEntity();
        activa.setId(UUID.randomUUID().toString());
        activa.setRuc("10101010101");
        activa.setRazonSocial("Activa");
        activa.setEstado("ACTIVA");
        activa.setCreadoEn(Instant.now());
        activa.setActualizadoEn(Instant.now());
        activa.setActivo(true);
        activa.setVersion(0L);

        EmpresaJpaEntity borrada = new EmpresaJpaEntity();
        borrada.setId(UUID.randomUUID().toString());
        borrada.setRuc("20202020202");
        borrada.setRazonSocial("Borrada");
        borrada.setEstado("ACTIVA");
        borrada.setCreadoEn(Instant.now());
        borrada.setActualizadoEn(Instant.now());
        borrada.setActivo(true);
        borrada.setVersion(0L);

        entityManager.persist(activa);
        entityManager.persist(borrada);
        entityManager.flush();

        // Borramos una
        entityManager.remove(borrada);
        entityManager.flush();
        entityManager.clear();

        List<EmpresaJpaEntity> todas = entityManager.createQuery("SELECT e FROM EmpresaJpaEntity e", EmpresaJpaEntity.class).getResultList();
        
        // Verifica que la lista no contiene la borrada
        boolean foundBorrada = todas.stream().anyMatch(e -> e.getId().equals(borrada.getId()));
        boolean foundActiva = todas.stream().anyMatch(e -> e.getId().equals(activa.getId()));

        assertFalse(foundBorrada, "JPQL devolvió una entidad borrada, @SQLRestriction falló");
        assertTrue(foundActiva, "JPQL debería devolver la entidad activa");
    }
}
