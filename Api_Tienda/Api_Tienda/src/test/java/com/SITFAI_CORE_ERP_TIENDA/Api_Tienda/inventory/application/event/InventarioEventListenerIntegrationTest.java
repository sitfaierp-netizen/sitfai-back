package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.TipoBodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.*;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.TicketEmitidoEvent;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.MetodoPago;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TicketVenta;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TurnoId;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = ApiTiendaApplication.class)
@ActiveProfiles("test")
@org.springframework.context.annotation.Import(TestcontainersConfiguration.class)
class InventarioEventListenerIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private BodegaRepository bodegaRepository;

    @Autowired
    private DescontarStockPorVentaEventHandler eventHandler;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private EmpresaId empresaId;
    private SucursalId sucursalId;
    private Bodega bodegaInicial;
    private ProductoId productoIdInv;

    @BeforeEach
    void setUp() {
        empresaId = new EmpresaId(UUID.randomUUID());
        sucursalId = new SucursalId(UUID.randomUUID());
        productoIdInv = new ProductoId(UUID.randomUUID());

        Bodega bodega = Bodega.crear(empresaId, sucursalId, "BOD-POS", "Bodega POS", TipoBodega.VENTA);
        
        bodega.registrarIngreso(
                productoIdInv, 
                Cantidad.de(new BigDecimal("100.0000")), 
                LoteId.de("LOTE-1"), 
                Instant.now().plusSeconds(3600), 
                new DocumentoFuenteId("INICIAL", "1")
        );
        bodegaInicial = bodegaRepository.guardar(bodega);
    }

    @AfterEach
    void tearDown() {
        // En un test real podriamos limpiar la base de datos, pero como se usa la DB de test y UUIDs aleatorios,
        // no colisionará.
    }

    @Test
    void cuandoSeEmiteTicket_entoncesSeDescuentaStockCorrectamente() {
        // Arrange
        com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.ProductoId productoIdPos = 
                new com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.ProductoId(productoIdInv.valor());
        
        TicketVenta ticket = TicketVenta.abrir(
                empresaId.valor(), 
                new CajaId(UUID.randomUUID()), 
                new TurnoId(UUID.randomUUID()), 
                "Cajero Test"
        );
        ticket.agregarLinea(productoIdPos, "Producto Test", new BigDecimal("10.0000"), new Dinero(new BigDecimal("15.0000")));
        ticket.pagar(MetodoPago.EFECTIVO);

        TicketEmitidoEvent event = (TicketEmitidoEvent) ticket.pullDomainEvents().stream()
                .filter(e -> e instanceof TicketEmitidoEvent)
                .findFirst()
                .orElseThrow();

        // Act
        // Publicar evento en transaccion para que el AFTER_COMMIT listener se ejecute
        transactionTemplate.execute(status -> {
            eventPublisher.publishEvent(event);
            return null;
        });

        // Assert
        Bodega bodegaFinal = bodegaRepository.buscarPorId(bodegaInicial.getId(), empresaId).orElseThrow();
        BigDecimal stockActual = bodegaFinal.consultarStock(productoIdInv);
        assertEquals(0, new BigDecimal("90.0000").compareTo(stockActual), "El stock debería haber disminuido a 90");
    }

    @Test
    void dosTicketsConcurrentes_lanzanOptimisticConcurrencyException() throws InterruptedException {
        // Arrange
        com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.ProductoId productoIdPos = 
                new com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.ProductoId(productoIdInv.valor());
        
        TicketVenta ticket = TicketVenta.abrir(
                empresaId.valor(), new CajaId(UUID.randomUUID()), new TurnoId(UUID.randomUUID()), "Cajero Test"
        );
        ticket.agregarLinea(productoIdPos, "Producto Test", new BigDecimal("10.0000"), new Dinero(new BigDecimal("15.0000")));
        ticket.pagar(MetodoPago.EFECTIVO);

        TicketEmitidoEvent event = (TicketEmitidoEvent) ticket.pullDomainEvents().stream()
                .filter(e -> e instanceof TicketEmitidoEvent)
                .findFirst()
                .orElseThrow();

        int numeroDeHilos = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numeroDeHilos);
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(numeroDeHilos);
        AtomicReference<Exception> excepcionAtrapada = new AtomicReference<>();

        Runnable tarea = () -> {
            try {
                latch.await();
                // Llamamos directamente al listener para forzar concurrencia exacta
                eventHandler.on(event);
            } catch (Exception e) {
                excepcionAtrapada.set(e);
            } finally {
                doneLatch.countDown();
            }
        };

        // Act
        executorService.submit(tarea);
        executorService.submit(tarea);

        latch.countDown();
        doneLatch.await();
        executorService.shutdown();

        // Assert
        assertNotNull(excepcionAtrapada.get(), "Debería haber lanzado una excepción de concurrencia");
        assertTrue(excepcionAtrapada.get().getClass().getName().contains("OptimisticLocking") ||
                   excepcionAtrapada.get().getClass().getName().contains("OptimisticConcurrencyException"),
                   "La excepción debe ser de tipo Optimistic Locking, pero fue " + excepcionAtrapada.get().getClass().getName());
    }
}
