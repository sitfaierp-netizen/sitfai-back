package com.SITFAI_CORE_ERP_TIENDA.core.integrity.softdelete;

import org.springframework.context.annotation.Import;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.SucursalJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.EmpresaJpaEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Import(com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration.class)
@SpringBootTest(classes = ApiTiendaApplication.class)
@ActiveProfiles("test")
class SoftDeleteTenantIsolationTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    @Transactional
    void softDeleteNoDebeAfectarEntidadesDeOtroTenant() {
        EmpresaJpaEntity empresa1 = new EmpresaJpaEntity();
        empresa1.setId(UUID.randomUUID().toString());
        empresa1.setRuc("T1-123");
        empresa1.setRazonSocial("Tenant 1");
        empresa1.setEstado("ACTIVA");
        empresa1.setCreadoEn(Instant.now());
        empresa1.setActualizadoEn(Instant.now());

        EmpresaJpaEntity empresa2 = new EmpresaJpaEntity();
        empresa2.setId(UUID.randomUUID().toString());
        empresa2.setRuc("T2-456");
        empresa2.setRazonSocial("Tenant 2");
        empresa2.setEstado("ACTIVA");
        empresa2.setCreadoEn(Instant.now());
        empresa2.setActualizadoEn(Instant.now());

        entityManager.persist(empresa1);
        entityManager.persist(empresa2);

        SucursalJpaEntity sucursal1 = new SucursalJpaEntity(
            UUID.randomUUID().toString(), empresa1, "SUC-01", "Central T1", "ACTIVO", Instant.now(), Instant.now(), true, null, null
        );
        SucursalJpaEntity sucursal2 = new SucursalJpaEntity(
            UUID.randomUUID().toString(), empresa2, "SUC-01", "Central T2", "ACTIVO", Instant.now(), Instant.now(), true, null, null
        );

        entityManager.persist(sucursal1);
        entityManager.persist(sucursal2);
        entityManager.flush();

        // Eliminar solo la del tenant 1
        entityManager.remove(sucursal1);
        entityManager.flush();
        entityManager.clear();

        // Verificar que la del tenant 1 esta inactiva (a nivel nativo) y la del tenant 2 esta activa
        Long countActivasT1 = (Long) entityManager.createNativeQuery("SELECT count(*) FROM core_sucursal WHERE empresa_id = :eid AND activo = 1").setParameter("eid", empresa1.getId()).getSingleResult();
        Long countActivasT2 = (Long) entityManager.createNativeQuery("SELECT count(*) FROM core_sucursal WHERE empresa_id = :eid AND activo = 1").setParameter("eid", empresa2.getId()).getSingleResult();

        assertEquals(0L, countActivasT1);
        assertEquals(1L, countActivasT2);
    }
}
