package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.IngresoStockRegistradoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.TipoBodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.SucursalId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.RecepcionarMercanciaWebRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de Integración: Inbound Logistics (Logística de Entrada).
 * <p>
 * Simula a un operador de bodega realizando una recepción física de mercancía amparada
 * en una Orden de Compra (BOD-04, MT-01) contra el endpoint REST:
 * {@code POST /api/v1/inventory/bodegas/{id}/recepciones}.
 * <p>
 * Verifica que:
 * 1. El controlador HTTP procesa la petición y retorna HTTP 201 Created.
 * 2. El stock disponible del producto aumenta en la base de datos viva.
 * 3. El evento de dominio {@link IngresoStockRegistradoEvent} es emitido.
 */
@Transactional
public class InboundLogisticsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BodegaRepository bodegaRepository;

    @Autowired
    private TestInboundEventListener eventListener;

    @MockitoBean
    private TenantProviderPort tenantProviderPort;

    private EmpresaId empresaId;
    private Bodega bodegaInicial;

    @BeforeEach
    void setUp() {
        eventListener.limpiar();

        empresaId = new EmpresaId(UUID.randomUUID());
        when(tenantProviderPort.getEmpresaIdAutenticada()).thenReturn(empresaId);

        // Crear y persistir una bodega activa para el tenant en la base de datos viva
        Bodega bodega = Bodega.crear(
                empresaId,
                new SucursalId(UUID.randomUUID()),
                "BOD-INBOUND-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase(),
                "Bodega Principal Recepción Inbound",
                TipoBodega.VENTA
        );
        bodegaInicial = bodegaRepository.guardar(bodega);
    }

    @Test
    @DisplayName("Debe recepcionar mercancía con éxito aumentando el stock disponible en BD viva")
    void recepcionarMercancia_conExito_aumentaStockEnBaseDeDatos() throws Exception {
        // GIVEN: Un operador en el andén con una Orden de Compra y un lote físico
        UUID ordenCompraId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();
        BigDecimal cantidadIngresada = new BigDecimal("150.00");
        Instant fechaCaducidad = Instant.now().plus(60, ChronoUnit.DAYS);

        var loteWeb = new RecepcionarMercanciaWebRequest.LoteIngresoWebRequest(
                productoId,
                cantidadIngresada,
                "LOT-PROV-2026-X",
                fechaCaducidad
        );

        var request = new RecepcionarMercanciaWebRequest(ordenCompraId, List.of(loteWeb));

        // Verificar stock previo en cero
        BigDecimal stockInicial = bodegaInicial.consultarStock(new ProductoId(productoId));
        assertThat(stockInicial).isEqualByComparingTo(BigDecimal.ZERO);

        // WHEN: El operador envía el POST al endpoint de recepciones
        mockMvc.perform(post("/api/v1/inventory/bodegas/" + bodegaInicial.getId().valor() + "/recepciones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bodegaId").value(bodegaInicial.getId().valor().toString()))
                .andExpect(jsonPath("$.ordenCompraId").value(ordenCompraId.toString()))
                .andExpect(jsonPath("$.totalLotesRecepcionados").value(1))
                .andExpect(jsonPath("$.mensaje").value("Mercancía recepcionada exitosamente en bodega"));

        // THEN: El stock disponible en la base de datos viva para el producto aumentó exactamente en la cantidad recepcionada
        Bodega bodegaActualizada = bodegaRepository.buscarPorId(bodegaInicial.getId(), empresaId)
                .orElseThrow(() -> new AssertionError("La bodega debe existir en BD"));

        BigDecimal stockActualizado = bodegaActualizada.consultarStock(new ProductoId(productoId));
        assertThat(stockActualizado).isEqualByComparingTo(cantidadIngresada);

        // THEN: Se verificó la publicación del evento IngresoStockRegistradoEvent
        assertThat(eventListener.getEventosIngreso())
                .hasSize(1)
                .anySatisfy(event -> {
                    assertThat(event.bodegaId()).isEqualTo(bodegaInicial.getId());
                    assertThat(event.empresaId()).isEqualTo(empresaId);
                    assertThat(event.documentoFuente().tipo()).isEqualTo("ORDEN_COMPRA");
                    assertThat(event.documentoFuente().numero()).isEqualTo(ordenCompraId.toString());
                    assertThat(event.totalLotes()).isEqualTo(1);
                });
    }

    @TestComponent
    public static class TestInboundEventListener {
        private final List<IngresoStockRegistradoEvent> eventosIngreso = new CopyOnWriteArrayList<>();

        @EventListener
        public void onIngresoStock(IngresoStockRegistradoEvent event) {
            eventosIngreso.add(event);
        }

        public List<IngresoStockRegistradoEvent> getEventosIngreso() {
            return eventosIngreso;
        }

        public void limpiar() {
            eventosIngreso.clear();
        }
    }
}
