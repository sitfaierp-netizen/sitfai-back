package com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.config.JpaAuditingConfig;
import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.test.context.TestPropertySource;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(classes = com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication.class)
@Import({JpaAuditingConfig.class, JpaAuditingIntegrationTest.Config.class})
class JpaAuditingIntegrationTest {

    @Autowired
    private org.springframework.data.repository.CrudRepository<com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.EmpresaJpaEntity, String> repository;

    @Test
    void debeEstamparAuditoriaAlGuardar() {
        var entity = new com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.EmpresaJpaEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setRuc("1234567890123");
        entity.setRazonSocial("Test Empresa");
        entity.setEstado("ACTIVO");
        entity.setActivo(true);

        var saved = repository.save(entity);

        assertNotNull(saved.getCreadoEn());
        assertEquals("test_actor", saved.getCreadoPor());
        assertNotNull(saved.getActualizadoEn());
        assertEquals("test_actor", saved.getActualizadoPor());
    }

    @TestConfiguration
    @EntityScan(basePackageClasses = com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.EmpresaJpaEntity.class)
    static class Config {
        @Bean
        public ActorProviderPort dummyActorProvider() {
            return () -> "test_actor";
        }
    }
}
