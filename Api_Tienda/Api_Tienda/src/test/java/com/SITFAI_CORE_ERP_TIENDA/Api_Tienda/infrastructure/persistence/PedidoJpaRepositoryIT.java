package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.persistence;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity.LineaPedidoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity.PedidoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.repository.PedidoJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de Integración de Infraestructura: {@link PedidoJpaRepository}.
 * <p>
 * Regla 8: @SpringBootTest + Testcontainers MySQL 8+, validando el esquema Flyway (V2) y el aislamiento multitenant (MT-01, MT-02).
 */
@SpringBootTest
@Transactional
@Testcontainers
@DisplayName("Infraestructura: PedidoJpaRepository con Testcontainers MySQL")
class PedidoJpaRepositoryIT {

    @Container
    private static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.4.0")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private PedidoJpaRepository repository;

    @Test
    @DisplayName("MT-01: Debe guardar y recuperar un Pedido con sus líneas por ID y EmpresaId")
    void debeGuardarYRecuperarPedido() {
        UUID id = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        Instant ahora = Instant.now();

        PedidoJpaEntity entity = new PedidoJpaEntity(
                id,
                empresaId,
                clienteId,
                "BORRADOR",
                new BigDecimal("50.00"),
                "USD",
                ahora,
                ahora
        );

        LineaPedidoJpaEntity linea = new LineaPedidoJpaEntity(
                UUID.randomUUID(),
                entity,
                empresaId,
                productoId,
                2,
                new BigDecimal("25.00"),
                "USD",
                new BigDecimal("50.00")
        );
        entity.agregarLinea(linea);

        repository.save(entity);

        Optional<PedidoJpaEntity> resultado = repository.findByIdAndEmpresaId(id, empresaId);

        assertTrue(resultado.isPresent());
        assertEquals("BORRADOR", resultado.get().getEstado());
        assertEquals(1, resultado.get().getLineas().size());
        assertEquals(productoId, resultado.get().getLineas().get(0).getProductoId());
        assertEquals(0, new BigDecimal("50.00").compareTo(resultado.get().getTotal()));
    }

    @Test
    @DisplayName("MT-02: Aislamiento Multitenant — No debe encontrar pedido con empresaId diferente")
    void noDebeEncontrarPedidoConOtraEmpresa() {
        UUID id = UUID.randomUUID();
        UUID empresaId1 = UUID.randomUUID();
        UUID empresaId2 = UUID.randomUUID();
        Instant ahora = Instant.now();

        PedidoJpaEntity entity = new PedidoJpaEntity(
                id,
                empresaId1,
                UUID.randomUUID(),
                "BORRADOR",
                BigDecimal.ZERO,
                "USD",
                ahora,
                ahora
        );

        repository.save(entity);

        Optional<PedidoJpaEntity> noEncontrado = repository.findByIdAndEmpresaId(id, empresaId2);
        assertFalse(noEncontrado.isPresent());
    }
}
