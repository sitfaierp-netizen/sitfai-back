package com.SITFAI_CORE_ERP_TIENDA.core.idempotency;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.adapter.in.web.Idempotent;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.adapter.out.persistence.SpringDataIdempotencyRepository;
import com.SITFAI_CORE_ERP_TIENDA.core.idempotency.infrastructure.adapter.out.persistence.entity.IdempotencyJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = ApiTiendaApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class IdempotencyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataIdempotencyRepository repository;

    @MockBean
    private TenantProviderPort tenantProviderPort;

    private static final String TENANT_ID = UUID.randomUUID().toString();
    private static final String TENANT_ID_2 = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        Mockito.when(tenantProviderPort.getEmpresaIdAutenticada()).thenReturn(com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId.de(TENANT_ID));
        DummyController.callCount.set(0);
    }

    @Test
    void test2_repeticionIdenticaDevuelveCacheado() throws Exception {
        String key = UUID.randomUUID().toString();
        String payload = "{\"data\":\"test\"}";

        // Primera petición
        mockMvc.perform(post("/api/dummy")
                        .header("Idempotency-Key", key)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string("DUMMY_OK"));

        assertEquals(1, DummyController.callCount.get());

        // Segunda petición idéntica
        mockMvc.perform(post("/api/dummy")
                        .header("Idempotency-Key", key)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(content().string("DUMMY_OK"));

        // Asegurar que el controlador NO se ejecutó de nuevo
        assertEquals(1, DummyController.callCount.get());
    }

    @Test
    void test11_restriccionUniqueEfectivaMysql() {
        String key = UUID.randomUUID().toString();
        IdempotencyJpaEntity e1 = new IdempotencyJpaEntity(UUID.randomUUID(), TENANT_ID, key, "hash1", "PROCESSING", null, null, Instant.now());
        repository.save(e1);

        IdempotencyJpaEntity e2 = new IdempotencyJpaEntity(UUID.randomUUID(), TENANT_ID, key, "hash2", "PROCESSING", null, null, Instant.now());
        assertThrows(DataIntegrityViolationException.class, () -> repository.saveAndFlush(e2));
    }

    @Test
    void test12_rollbackTransaccional() throws Exception {
        String key = UUID.randomUUID().toString();
        String payload = "{\"data\":\"fail\"}";

        // Esta petición forzará una excepción en el controlador
        mockMvc.perform(post("/api/dummy")
                        .header("Idempotency-Key", key)
                        .contentType("application/json")
                        .content(payload))
                .andExpect(status().isInternalServerError());

        // Verificar que el estado quedó como FAILED
        IdempotencyJpaEntity saved = repository.findByEmpresaIdAndIdempotencyKey(TENANT_ID, key).get();
        assertEquals("FAILED", saved.getStatus());
        assertEquals(500, saved.getHttpStatus());
    }

    @Test
    void test6_concurrenciaSimultanea() throws Exception {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        String key = UUID.randomUUID().toString();
        String payload = "{\"data\":\"test\"}";

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    latch.await();
                    mockMvc.perform(post("/api/dummy")
                                    .header("Idempotency-Key", key)
                                    .contentType("application/json")
                                    .content(payload))
                            .andDo(result -> {
                                if (result.getResponse().getStatus() == 200) {
                                    successCount.incrementAndGet();
                                }
                            });
                } catch (Exception e) {
                    // Ignorar
                } finally {
                    done.countDown();
                }
            });
        }

        latch.countDown();
        done.await();

        // En un entorno altamente concurrente real esto requeriría locks de BD, 
        // pero la prueba demuestra que eventual consistency y Unique Constraint
        // evitarán dobles ejecuciones efectivas.
        assertEquals(1, DummyController.callCount.get());
    }
}

@RestController
class DummyController {
    public static final AtomicInteger callCount = new AtomicInteger(0);

    @PostMapping("/api/dummy")
    @Idempotent
    public ResponseEntity<String> dummyEndpoint(@RequestBody String body) {
        if (body.contains("fail")) {
            throw new RuntimeException("Forced failure");
        }
        callCount.incrementAndGet();
        return ResponseEntity.ok("DUMMY_OK");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        return ResponseEntity.status(500).body(e.getMessage());
    }
}
