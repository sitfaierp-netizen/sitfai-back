package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.TipoBodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.SucursalId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.adapter.BodegaJpaAdapter;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.repository.BodegaJpaRepository;
import com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception.OptimisticConcurrencyException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication.class)
@ActiveProfiles("test")
class BodegaConcurrencyTest {

    @Autowired
    private BodegaJpaAdapter bodegaJpaAdapter;

    @Autowired
    private BodegaJpaRepository bodegaJpaRepository;

    private EmpresaId empresaId;
    private SucursalId sucursalId;
    private Bodega bodegaInicial;

    @BeforeEach
    void setUp() {
        bodegaJpaRepository.deleteAll();

        empresaId = new EmpresaId(UUID.randomUUID());
        sucursalId = new SucursalId(UUID.randomUUID());

        Bodega bodega = Bodega.crear(empresaId, sucursalId, "BOD-CONC", "Bodega Concurrente", TipoBodega.VENTA);
        bodegaInicial = bodegaJpaAdapter.guardar(bodega);
    }

    @AfterEach
    void tearDown() {
        bodegaJpaRepository.deleteAll();
    }

    @Test
    void guardar_dosHilosModificandoLaMismaBodega_lanzaOptimisticConcurrencyException() throws InterruptedException {
        // Arrange
        int numeroDeHilos = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numeroDeHilos);
        CountDownLatch latch = new CountDownLatch(1); // Para que ambos hilos empiecen al mismo tiempo
        CountDownLatch doneLatch = new CountDownLatch(numeroDeHilos); // Para esperar a que ambos terminen

        AtomicReference<Exception> excepcionAtrapada = new AtomicReference<>();
        AtomicReference<Bodega> bodegaModificada = new AtomicReference<>();

        Runnable tarea = () -> {
            try {
                // Ambos hilos leen exactamente la misma versión
                Bodega bodegaLeida = bodegaJpaAdapter.buscarPorId(bodegaInicial.getId(), empresaId).orElseThrow();
                
                // Esperar a que el otro hilo también esté listo para generar colisión
                latch.await();

                // Modificar el agregado de alguna forma
                bodegaLeida.activar(); // Operación idéntica
                
                // Intentar guardar
                Bodega guardada = bodegaJpaAdapter.guardar(bodegaLeida);
                bodegaModificada.set(guardada);
            } catch (Exception e) {
                excepcionAtrapada.set(e);
            } finally {
                doneLatch.countDown();
            }
        };

        // Act
        executorService.submit(tarea);
        executorService.submit(tarea);

        // Dar señal de salida
        latch.countDown();

        // Esperar a que ambos hilos terminen
        doneLatch.await();
        executorService.shutdown();

        if (excepcionAtrapada.get() != null) {
            excepcionAtrapada.get().printStackTrace();
        }
        assertNotNull(excepcionAtrapada.get(), "Debería haber lanzado una excepción en el segundo hilo");
        assertTrue(excepcionAtrapada.get() instanceof OptimisticConcurrencyException, 
                "La excepción debe ser OptimisticConcurrencyException de nuestro dominio, pero fue: " + (excepcionAtrapada.get() != null ? excepcionAtrapada.get().getClass().getName() : "null"));
        
        Bodega bodegaFinal = bodegaJpaAdapter.buscarPorId(bodegaInicial.getId(), empresaId).orElseThrow();
        assertEquals(1L, bodegaFinal.getVersion(), "La versión debió incrementarse a 1 después del primer guardado exitoso");
    }
}
