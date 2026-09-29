package com.SITFAI_CORE_ERP_TIENDA.replenishment;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.StockDescontadoPorVentaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.event.NecesidadAbastecimientoDetectadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.PoliticaInventario;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.NivelOptimo;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.PoliticaId;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.PuntoReorden;
import com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.port.output.PoliticaInventarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Prueba de Integración: Módulo Replenishment (Piloto Automático de Inventario).
 * <p>
 * Certifica la cadena completa:
 * 1. Una política se persiste en BD con aislamiento multi-tenant.
 * 2. Un evento de disminución de stock ({@link StockDescontadoPorVentaEvent}) se publica.
 * 3. El sensor asíncrono {@code StockMovimientoEventListener} intercepta el evento.
 * 4. El caso de uso evalúa el stock y emite {@link NecesidadAbastecimientoDetectadaEvent}.
 */
@Transactional
public class ReplenishmentIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private PoliticaInventarioRepository politicaRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;


    private UUID empresaId;
    private UUID bodegaId;
    private UUID productoId;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
        bodegaId  = UUID.randomUUID();
        productoId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Debe emitir NecesidadAbastecimientoDetectadaEvent cuando el stock perfora el PuntoReorden")
    void debeEmitirEventoCuandoStockPerfora() throws InterruptedException {
        // GIVEN: Una política activa con PuntoReorden=10 y NivelOptimo=50
        PoliticaInventario politica = new PoliticaInventario(
                new PoliticaId(UUID.randomUUID()),
                new EmpresaId(empresaId),
                new com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.BodegaId(bodegaId),
                new com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.ProductoId(productoId),
                new PuntoReorden(10),
                new NivelOptimo(50),
                true
        );
        politicaRepository.guardar(politica);

        // WHEN: Se publica un evento de disminución de stock con cantidad=5 (perfora PuntoReorden=10)
        StockDescontadoPorVentaEvent stockEvent = StockDescontadoPorVentaEvent.of(
                new com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId(empresaId),
                new BodegaId(bodegaId),
                new ProductoId(productoId),
                new Cantidad(BigDecimal.valueOf(5)),
                new DocumentoFuenteId("POS_VENTA", UUID.randomUUID().toString())
        );
        eventPublisher.publishEvent(stockEvent);

        // THEN: El sensor asíncrono debe capturar el evento de necesidad de abastecimiento
        org.mockito.Mockito.verify(applicationEventPublisher, org.mockito.Mockito.times(1))
            .publishEvent(org.mockito.ArgumentMatchers.any(NecesidadAbastecimientoDetectadaEvent.class));
    }

    @Test
    @DisplayName("NO debe emitir evento cuando la política está inactiva")
    void noDebeEmitirEventoCuandoPoliticaInactiva() throws InterruptedException {
        // GIVEN: Una política INACTIVA
        PoliticaInventario politicaInactiva = new PoliticaInventario(
                new PoliticaId(UUID.randomUUID()),
                new EmpresaId(empresaId),
                new com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.BodegaId(bodegaId),
                new com.SITFAI_CORE_ERP_TIENDA.replenishment.domain.model.politica.vo.ProductoId(productoId),
                new PuntoReorden(10),
                new NivelOptimo(50),
                false // INACTIVA
        );
        politicaRepository.guardar(politicaInactiva);

        // WHEN: Se publica evento de stock (que perforaría el umbral si estuviera activa)
        StockDescontadoPorVentaEvent stockEvent = StockDescontadoPorVentaEvent.of(
                new com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId(empresaId),
                new BodegaId(bodegaId),
                new ProductoId(productoId),
                new Cantidad(BigDecimal.valueOf(3)),
                new DocumentoFuenteId("POS_VENTA", UUID.randomUUID().toString())
        );
        eventPublisher.publishEvent(stockEvent);

        // THEN: Esperamos un tiempo razonable y no debe haber ningún evento de necesidad
        Thread.sleep(1000);
        org.mockito.Mockito.verify(applicationEventPublisher, org.mockito.Mockito.never())
            .publishEvent(org.mockito.ArgumentMatchers.any(NecesidadAbastecimientoDetectadaEvent.class));
    }

}
