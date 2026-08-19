package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pruebas de Integración de Infraestructura: {@link UsuarioJpaRepository}.
 * <p>
 * Regla 8: @SpringBootTest + Testcontainers MySQL, validando esquema Flyway (V6) y aislamiento multitenant (MT-01).
 * Nota: Se especifica classes = ApiTiendaApplication.class porque este módulo vive fuera del paquete raíz
 * del componente @SpringBootApplication (com.SITFAI_CORE_ERP_TIENDA.Api_Tienda).
 */
@SpringBootTest(classes = ApiTiendaApplication.class)
@Transactional
@Testcontainers
@DisplayName("Infraestructura: UsuarioJpaRepository con Testcontainers MySQL (Flyway V6)")
class UsuarioJpaRepositoryIT {

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
    private UsuarioJpaRepository repository;

    @Test
    @DisplayName("MT-01: Debe guardar y recuperar un Usuario por ID y EmpresaId")
    void debeGuardarYRecuperarUsuario() {
        String id = UUID.randomUUID().toString();
        String empresaId = UUID.randomUUID().toString();
        Instant ahora = Instant.now();

        UsuarioJpaEntity entity = new UsuarioJpaEntity(
                id,
                empresaId,
                "cajero01",
                "cajero01@empresa.com",
                "CAJERO",
                "ACTIVO",
                ahora,
                ahora,
                true,
                null,
                null
        );

        repository.save(entity);

        Optional<UsuarioJpaEntity> recuperado = repository.findByEmpresaIdAndId(empresaId, id);

        assertThat(recuperado).isPresent();
        assertThat(recuperado.get().getUsername()).isEqualTo("cajero01");
        assertThat(recuperado.get().getEmail()).isEqualTo("cajero01@empresa.com");
        assertThat(recuperado.get().getRol()).isEqualTo("CAJERO");
        assertThat(recuperado.get().getEstado()).isEqualTo("ACTIVO");
    }

    @Test
    @DisplayName("MT-01: Aislamiento Multitenant — No debe encontrar usuario con empresaId diferente")
    void noDebeEncontrarConOtraEmpresa() {
        String id = UUID.randomUUID().toString();
        String empresaId1 = UUID.randomUUID().toString();
        String empresaId2 = UUID.randomUUID().toString();
        Instant ahora = Instant.now();

        UsuarioJpaEntity entity = new UsuarioJpaEntity(
                id,
                empresaId1,
                "admin01",
                "admin01@empresa.com",
                "EMPRESA_ADMIN",
                "ACTIVO",
                ahora,
                ahora,
                true,
                null,
                null
        );

        repository.save(entity);

        Optional<UsuarioJpaEntity> noEncontrado = repository.findByEmpresaIdAndId(empresaId2, id);
        assertThat(noEncontrado).isNotPresent();
    }

    @Test
    @DisplayName("Debe buscar por username y email respetando el tenant")
    void debeBuscarPorUsernameYEmail() {
        String id = UUID.randomUUID().toString();
        String empresaId = UUID.randomUUID().toString();
        Instant ahora = Instant.now();

        UsuarioJpaEntity entity = new UsuarioJpaEntity(
                id,
                empresaId,
                "operador_bodega",
                "bodega@empresa.com",
                "BODEGA_OPERATOR",
                "ACTIVO",
                ahora,
                ahora,
                true,
                null,
                null
        );

        repository.save(entity);

        assertThat(repository.findByEmpresaIdAndUsername(empresaId, "operador_bodega")).isPresent();
        assertThat(repository.findByEmpresaIdAndEmail(empresaId, "bodega@empresa.com")).isPresent();
        assertThat(repository.existsByEmpresaIdAndUsername(empresaId, "operador_bodega")).isTrue();
        assertThat(repository.existsByEmpresaIdAndEmail(empresaId, "bodega@empresa.com")).isTrue();

        List<UsuarioJpaEntity> lista = repository.findByEmpresaId(empresaId);
        assertThat(lista).hasSize(1);
    }
}
