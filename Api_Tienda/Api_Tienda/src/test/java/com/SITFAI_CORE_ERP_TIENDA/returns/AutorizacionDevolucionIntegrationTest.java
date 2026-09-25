package com.SITFAI_CORE_ERP_TIENDA.returns;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.returns.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.AutorizacionDevolucion;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.port.output.AutorizacionDevolucionRepository;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.DevolucionId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.EstadoRma;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.event.ProductoAprobadoParaReingresoEvent;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.event.ProductoRechazadoAMermaEvent;
import com.SITFAI_CORE_ERP_TIENDA.returns.infrastructure.adapter.in.web.dto.CrearRmaWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.returns.infrastructure.adapter.in.web.dto.InspeccionarRmaWebRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ApiTiendaApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
@Transactional
public class AutorizacionDevolucionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AutorizacionDevolucionRepository repository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TenantProviderPort tenantProviderPort;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;

    private UUID empresaId;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
        when(tenantProviderPort.getEmpresaIdAutenticada()).thenReturn(EmpresaId.de(empresaId));
    }

    @Test
    @DisplayName("Crear RMA, recibir físicamente e inspeccionar lineas con dualidad de eventos")
    void flujoCompletoLogisticaInversa() throws Exception {
        UUID documentoId = UUID.randomUUID();
        UUID producto1 = UUID.randomUUID();
        UUID producto2 = UUID.randomUUID();
        
        // Reflection to instantiate nested record correctly for Web DTOs
        // Or we can just use strings directly, but let's build the JSON string to avoid compilation weirdness with inner records
        String createJson = """
            {
                "documentoFuenteId": "%s",
                "lineas": [
                    { "productoId": "%s", "cantidad": 2, "motivo": "Defecto" },
                    { "productoId": "%s", "cantidad": 1, "motivo": "No me gustó" }
                ]
            }
        """.formatted(documentoId, producto1, producto2);

        // 1. Crear y Recibir
        String responseContent = mockMvc.perform(post("/returns/rma")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("RECIBIDA_EN_CUARENTENA"))
                .andReturn().getResponse().getContentAsString();

        // Extract ID
        String devolucionIdStr = responseContent.split("\"devolucionId\":\"")[1].split("\"")[0];
        UUID devolucionId = UUID.fromString(devolucionIdStr);

        // 2. Inspeccionar Producto 1 (Aprobado)
        String inspectJsonAprobado = """
            {
                "productoId": "%s",
                "aprobado": true
            }
        """.formatted(producto1);

        mockMvc.perform(post("/returns/rma/{id}/inspeccionar", devolucionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inspectJsonAprobado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INSPECCION_PARCIAL"));

        // 3. Inspeccionar Producto 2 (Rechazado)
        String inspectJsonRechazado = """
            {
                "productoId": "%s",
                "aprobado": false
            }
        """.formatted(producto2);

        mockMvc.perform(post("/returns/rma/{id}/inspeccionar", devolucionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(inspectJsonRechazado))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INSPECCION_PARCIAL")); // Since it has 1 approved and 1 rejected, it stays as PARCIAL per logic.

        // 4. Verificaciones
        AutorizacionDevolucion rmaDb = repository.buscarPorId(EmpresaId.de(empresaId), DevolucionId.de(devolucionId)).orElseThrow();
        assertThat(rmaDb.getEstado()).isEqualTo(EstadoRma.INSPECCION_PARCIAL);
        
        // Check Event propagation
        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());
        
        List<Object> allEvents = captor.getAllValues();
        assertThat(allEvents).hasSize(2);
        
        // 1er Evento
        assertThat(allEvents.get(0)).isInstanceOf(ProductoAprobadoParaReingresoEvent.class);
        var event1 = (ProductoAprobadoParaReingresoEvent) allEvents.get(0);
        assertThat(event1.productoId().valor()).isEqualTo(producto1);

        // 2do Evento
        assertThat(allEvents.get(1)).isInstanceOf(ProductoRechazadoAMermaEvent.class);
        var event2 = (ProductoRechazadoAMermaEvent) allEvents.get(1);
        assertThat(event2.productoId().valor()).isEqualTo(producto2);
    }
}
