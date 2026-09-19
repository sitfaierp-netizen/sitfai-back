package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.RecepcionConfirmadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.LineaRecepcion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.Recepcion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.Lote;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.RecepcionId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.RecepcionRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.SucursalId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ApiTiendaApplication.class)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
class IngresoStockEventListenerIntegrationTest {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private BodegaRepository bodegaRepository;

    @Autowired
    private RecepcionRepository recepcionRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private EmpresaId empresaId;
    private BodegaId bodegaId;
    private ProductoId productoId;
    private RecepcionId recepcionId;

    @BeforeEach
    void setUp() {
        empresaId = new EmpresaId(UUID.randomUUID());
        bodegaId = BodegaId.nuevo();
        productoId = new ProductoId(UUID.randomUUID());
        recepcionId = RecepcionId.generar();

        Bodega bodega = Bodega.crear(empresaId, new SucursalId(UUID.randomUUID()), "BDG-TEST", "Bodega Test");
        bodegaId = bodega.getId();
        bodegaRepository.guardar(bodega);

        Recepcion recepcion = Recepcion.crearBorrador(
                recepcionId,
                empresaId,
                bodegaId,
                new OrdenCompraId(UUID.randomUUID()),
                "USER"
        );
        Lote lote = new Lote("LOTE-456", LocalDate.now().plusDays(60));
        recepcion.agregarLinea(new LineaRecepcion(productoId, Cantidad.de(BigDecimal.valueOf(500)), lote));
        recepcionRepository.guardar(recepcion);
    }

    @Test
    void cuandoSeConfirmaRecepcion_seDebeIncrementarStockAsincronamente() throws InterruptedException {
        // Arrange
        RecepcionConfirmadaEvent event = RecepcionConfirmadaEvent.of(empresaId, recepcionId);

        // Act
        transactionTemplate.executeWithoutResult(status -> {
            eventPublisher.publishEvent(event);
        });

        // Espera para dar tiempo al hilo @Async
        Thread.sleep(1000);

        // Assert
        Optional<Bodega> bodegaOpt = bodegaRepository.buscarPorId(bodegaId, empresaId);
        assertTrue(bodegaOpt.isPresent());
        Bodega bodega = bodegaOpt.get();
        
        assertEquals(0, BigDecimal.valueOf(500).compareTo(bodega.consultarStock(productoId)));
        assertEquals(1, bodega.getLotes().size());
        assertEquals("LOTE-456", bodega.getLotes().get(0).getLoteId().valor());
    }
}
