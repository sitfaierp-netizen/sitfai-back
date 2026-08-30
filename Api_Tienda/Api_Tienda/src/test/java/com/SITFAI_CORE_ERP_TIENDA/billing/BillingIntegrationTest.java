package com.SITFAI_CORE_ERP_TIENDA.billing;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.Factura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.Ruc;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.port.output.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Dinero;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
public class BillingIntegrationTest {

    @Autowired
    private FacturaRepository facturaRepository;

    @Test
    public void testPersistenciaYConcurrenciaOptimista() throws InterruptedException {
        // 1. Arrange: Crear y persistir factura
        UUID empresaId = UUID.randomUUID();
        FacturaId facturaId = new FacturaId(UUID.randomUUID());
        ClienteId clienteId = new ClienteId(UUID.randomUUID());
        Ruc ruc = new Ruc("0912345678001");
        
        Factura factura = Factura.crear(facturaId, empresaId, clienteId, null, ruc, "user1");
        factura.agregarLinea("Laptop", new BigDecimal("2"), new Dinero(new BigDecimal("1000.00")));
        facturaRepository.save(factura);

        // 2. Arrange Concurrencia
        int numHilos = 3;
        ExecutorService executor = Executors.newFixedThreadPool(numHilos);
        CountDownLatch latch = new CountDownLatch(numHilos);
        AtomicInteger exitos = new AtomicInteger(0);
        AtomicInteger fallos = new AtomicInteger(0);

        // 3. Act: Simular ataques concurrentes (todos intentan agregar una línea y emitir)
        for (int i = 0; i < numHilos; i++) {
            executor.submit(() -> {
                try {
                    // Cada hilo carga la misma versión de la factura
                    Factura facturaCargada = facturaRepository.findById(facturaId, empresaId).orElseThrow();
                    
                    facturaCargada.agregarLinea("Mouse", new BigDecimal("1"), new Dinero(new BigDecimal("25.00")));
                    facturaCargada.emitir(); // Cambia el estado a EMITIDO
                    
                    // Al guardar, se incrementa la versión. Solo 1 debería ganar.
                    facturaRepository.save(facturaCargada);
                    exitos.incrementAndGet();
                } catch (OptimisticLockingFailureException e) {
                    fallos.incrementAndGet();
                } catch (Exception e) {
                    System.out.println("Otro error: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await();

        // 4. Assert: Solo 1 éxito, los demás fallan por bloqueo optimista
        assertThat(exitos.get()).isEqualTo(1);
        assertThat(fallos.get()).isEqualTo(numHilos - 1);
        
        // Verificar que el estado final es EMITIDO
        Factura facturaFinal = facturaRepository.findById(facturaId, empresaId).orElseThrow();
        assertThat(facturaFinal.getEstado().name()).isEqualTo("EMITIDO");
        assertThat(facturaFinal.getVersion()).isGreaterThan(0L);
    }
}
