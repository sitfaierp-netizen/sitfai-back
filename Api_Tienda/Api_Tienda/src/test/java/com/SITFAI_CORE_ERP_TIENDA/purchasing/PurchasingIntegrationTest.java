package com.SITFAI_CORE_ERP_TIENDA.purchasing;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.CurrentActorProvider;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.OrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.event.OrdenCompraEmitidaEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.port.output.OrdenCompraRepository;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.EstadoOrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.web.dto.EmitirOrdenCompraWebRequest;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de Integración End-to-End: Bounded Context Purchasing (Puerto 8087).
 * <p>
 * Certifica que:
 * 1. El endpoint HTTP {@code POST /api/v1/purchasing/ordenes} procesa la emisión con éxito (201 Created).
 * 2. La Orden de Compra y sus líneas se persisten atómicamente en MySQL con aislamiento multitenant (MT-01).
 * 3. El evento de dominio {@link OrdenCompraEmitidaEvent} se dispara y entrega en el bus de Spring.
 */
@SpringBootTest(classes = ApiTiendaApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({TestcontainersConfiguration.class, PurchasingIntegrationTest.TestPurchasingEventListener.class})
@ActiveProfiles("test")
@Transactional
public class PurchasingIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrdenCompraRepository ordenCompraRepository;

    @Autowired
    private TestPurchasingEventListener eventListener;

    @MockitoBean
    private TenantProviderPort tenantProviderPort;

    @MockitoBean
    private CurrentActorProvider currentActorProvider;

    @TestComponent
    public static class TestPurchasingEventListener {
        private final List<OrdenCompraEmitidaEvent> events = new CopyOnWriteArrayList<>();

        @EventListener
        public void onOrdenEmitida(OrdenCompraEmitidaEvent event) {
            events.add(event);
        }

        public List<OrdenCompraEmitidaEvent> getEvents() {
            return events;
        }

        public void clear() {
            events.clear();
        }
    }

    @BeforeEach
    void setUp() {
        eventListener.clear();
    }

    @Test
    @DisplayName("Debe emitir orden de compra vía REST, persistir en MySQL con MT-01 y disparar OrdenCompraEmitidaEvent")
    void debeEmitirOrdenCompraYDispararEventoDeDominio() throws Exception {
        // 1. ARRANGE
        UUID empresaIdRaw = UUID.randomUUID();
        UUID proveedorIdRaw = UUID.randomUUID();
        UUID productoId1 = UUID.randomUUID();
        UUID productoId2 = UUID.randomUUID();

        EmpresaId empresaId = EmpresaId.de(empresaIdRaw);
        when(tenantProviderPort.getEmpresaIdAutenticada()).thenReturn(empresaId);
        when(currentActorProvider.getActorActual()).thenReturn("JEFE_COMPRAS_USER");

        EmitirOrdenCompraWebRequest request = new EmitirOrdenCompraWebRequest(
                proveedorIdRaw,
                List.of(
                        new EmitirOrdenCompraWebRequest.LineaOrdenCompraWebRequest(productoId1, 10, new BigDecimal("150.5000")),
                        new EmitirOrdenCompraWebRequest.LineaOrdenCompraWebRequest(productoId2, 5, new BigDecimal("200.0000"))
                )
        );

        // 2. ACT: Invocar POST /api/v1/purchasing/ordenes
        MvcResult result = mockMvc.perform(post("/api/v1/purchasing/ordenes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.empresaId").value(empresaIdRaw.toString()))
                .andExpect(jsonPath("$.proveedorId").value(proveedorIdRaw.toString()))
                .andExpect(jsonPath("$.estado").value("EMITIDA"))
                .andExpect(jsonPath("$.costoTotal").value(2505.0))
                .andExpect(jsonPath("$.lineas").isArray())
                .andReturn();

        // 3. ASSERT: Verificación en Base de Datos (Persistencia y aislamiento MT-01)
        String responseJson = result.getResponse().getContentAsString();
        String ordenIdStr = objectMapper.readTree(responseJson).get("id").asText();
        OrdenCompraId ordenCompraId = OrdenCompraId.de(ordenIdStr);

        Optional<OrdenCompra> ordenGuardadaOpt = ordenCompraRepository.buscarPorId(ordenCompraId, empresaId);
        assertThat(ordenGuardadaOpt).isPresent();

        OrdenCompra ordenGuardada = ordenGuardadaOpt.get();
        assertThat(ordenGuardada.getEstado()).isEqualTo(EstadoOrdenCompra.EMITIDA);
        assertThat(ordenGuardada.getEmpresaId()).isEqualTo(empresaId);
        assertThat(ordenGuardada.getProveedorId().valor()).isEqualTo(proveedorIdRaw);
        assertThat(ordenGuardada.getLineas()).hasSize(2);
        assertThat(ordenGuardada.calcularTotalEsperado()).isEqualByComparingTo("2505.0000");
        assertThat(ordenGuardada.getEmitidoEn()).isNotNull();

        // 4. ASSERT: Verificación de disparo del Evento de Dominio en el bus de Spring
        assertThat(eventListener.getEvents()).hasSize(1);
        OrdenCompraEmitidaEvent evento = eventListener.getEvents().get(0);
        assertThat(evento.empresaId()).isEqualTo(empresaId);
        assertThat(evento.ordenCompraId()).isEqualTo(ordenCompraId);
        assertThat(evento.proveedorId().valor()).isEqualTo(proveedorIdRaw);
        assertThat(evento.totalLineas()).isEqualTo(2);
        assertThat(evento.montoTotalEsperado()).isEqualByComparingTo("2505.0000");
    }
}
