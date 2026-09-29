package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.TipoBodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.Despacho;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.EstadoDespacho;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.DespachoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.LoteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.SucursalId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.ConfirmarDespachoWebRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de Integración: Logística de Salida (Outbound Logistics) y Cierre de Inventario.
 * <p>
 * Simula el despacho de un pedido a través del endpoint HTTP {@code POST /api/v1/inventory/despachos}
 * y certifica que:
 * 1. El controlador HTTP procesa el despacho y retorna HTTP 201 Created con estado DESPACHADO.
 * 2. El agregado Despacho se persiste en la base de datos viva con aislamiento MT-01.
 * 3. El oyente de eventos (DespachoConfirmadoEventHandler) deduce definitivamente las unidades físicas
 *    reservadas en la Bodega viva, disminuyendo el stock reservado a cero.
 */
@Transactional
public class OutboundLogisticsIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BodegaRepository bodegaRepository;

    @Autowired
    private DespachoRepository despachoRepository;

    @MockitoBean
    private TenantProviderPort tenantProviderPort;

    private EmpresaId empresaId;
    private Bodega bodegaInicial;
    private ProductoId productoId;

    @BeforeEach
    void setUp() {
        empresaId = EmpresaId.de(UUID.randomUUID());
        when(tenantProviderPort.getEmpresaIdAutenticada()).thenReturn(empresaId);

        productoId = ProductoId.de(UUID.randomUUID());

        // 1. Crear Bodega viva para el tenant
        Bodega bodega = Bodega.crear(
                empresaId,
                new SucursalId(UUID.randomUUID()),
                "BDG-OUT-" + UUID.randomUUID().toString().substring(0, 5).toUpperCase(),
                "Bodega Despachos Outbound",
                TipoBodega.VENTA
        );

        // 2. Abastecer 100 unidades físicas iniciales
        bodega.registrarIngreso(
                productoId,
                Cantidad.de(new BigDecimal("100.00")),
                LoteId.de("LOT-OUT-001"),
                Instant.now().plus(90, ChronoUnit.DAYS),
                new DocumentoFuenteId("RECEPCION", UUID.randomUUID().toString())
        );

        // 3. Persistir bodega inicial
        bodegaInicial = bodegaRepository.guardar(bodega);
    }

    @Test
    @DisplayName("Debe despachar pedido con éxito y disminuir el stock reservado de la Bodega viva")
    void despacharPedido_conExito_disminuyeStockReservadoEnBaseDeDatos() throws Exception {
        // GIVEN: Un pedido que tiene 35 unidades reservadas en la Bodega
        UUID pedidoId = UUID.randomUUID();
        BigDecimal cantidadADespachar = new BigDecimal("35.00");

        bodegaInicial.reservarStock(
                productoId,
                Cantidad.de(cantidadADespachar),
                new DocumentoFuenteId("PEDIDO", pedidoId.toString())
        );
        bodegaInicial = bodegaRepository.guardar(bodegaInicial);

        // Validar estado previo a la salida: Stock disponible = 65, Reservado = 35
        assertThat(bodegaInicial.consultarStock(productoId)).isEqualByComparingTo(new BigDecimal("65.00"));
        assertThat(bodegaInicial.consultarStockReservado(productoId)).isEqualByComparingTo(new BigDecimal("35.00"));

        // GIVEN: Operador en andén confirma el despacho físico del pedido
        var lineaWeb = new ConfirmarDespachoWebRequest.LineaDespachoWebRequest(
                productoId.valor(),
                cantidadADespachar
        );

        var request = new ConfirmarDespachoWebRequest(
                pedidoId,
                bodegaInicial.getId().valor(),
                List.of(lineaWeb)
        );

        // WHEN: Envío del POST al endpoint REST de despachos
        mockMvc.perform(post("/api/v1/inventory/despachos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pedidoId").value(pedidoId.toString()))
                .andExpect(jsonPath("$.bodegaId").value(bodegaInicial.getId().valor().toString()))
                .andExpect(jsonPath("$.estado").value("DESPACHADO"))
                .andExpect(jsonPath("$.totalLineas").value(1));

        // THEN 1: El Despacho quedó persistido en estado DESPACHADO en base de datos viva
        Optional<Despacho> despachoPersistido = despachoRepository.buscarPorPedidoId(PedidoId.de(pedidoId), empresaId);
        assertThat(despachoPersistido).isPresent();
        assertThat(despachoPersistido.get().getEstado()).isEqualTo(EstadoDespacho.DESPACHADO);
        assertThat(despachoPersistido.get().getLineas()).hasSize(1);

        // THEN 2: El stock RESERVADO de la Bodega disminuyó a CERO en la base de datos viva
        Bodega bodegaActualizada = bodegaRepository.buscarPorId(bodegaInicial.getId(), empresaId)
                .orElseThrow(() -> new AssertionError("La bodega debe existir en BD"));

        assertThat(bodegaActualizada.consultarStockReservado(productoId))
                .as("El stock reservado debe haber sido deducido definitivamente")
                .isEqualByComparingTo(BigDecimal.ZERO);

        assertThat(bodegaActualizada.consultarStock(productoId))
                .as("El stock disponible permanece en las unidades no comprometidas")
                .isEqualByComparingTo(new BigDecimal("65.00"));
    }
}
