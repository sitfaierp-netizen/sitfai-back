package com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.config.JpaAuditingConfig;
import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.springframework.test.context.TestPropertySource;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;

class JpaAuditingIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private org.springframework.data.repository.CrudRepository<com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity.EmpresaJpaEntity, String> repository;

    @Test
    void debeEstamparAuditoriaAlGuardar() {
        org.mockito.Mockito.when(auditActorProviderPort.getCurrentActorId()).thenReturn("test_actor");
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

}
