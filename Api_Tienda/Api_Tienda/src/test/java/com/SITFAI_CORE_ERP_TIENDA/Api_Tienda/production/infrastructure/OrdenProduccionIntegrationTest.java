package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;

import org.springframework.context.annotation.Import;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.event.ProduccionCompletadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.event.ProduccionIniciadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.persistence.OrdenProduccionSpringDataRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.persistence.entity.OrdenProduccionJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class OrdenProduccionIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrdenProduccionSpringDataRepository repository;

    @Autowired
    private ApplicationEvents applicationEvents;



    private final UUID EMPRESA_ID = UUID.randomUUID();
    private final UUID RECETA_ID = UUID.randomUUID();
    private final UUID BODEGA_ID = UUID.randomUUID();
    private final Integer CANTIDAD = 50;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        Mockito.when(productionTenantProviderPort.getEmpresaIdAutenticada())
               .thenReturn(new com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.EmpresaId(EMPRESA_ID));
    }

    @Test
    void debePlanificarIniciarYCompletarOrden() throws Exception {
        // 1. Planificar Orden
        String planificarBody = String.format("{\"recetaId\":\"%s\", \"bodegaId\":\"%s\", \"cantidadProducir\": %d}", RECETA_ID, BODEGA_ID, CANTIDAD);
        MvcResult result = mockMvc.perform(post("/production/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(planificarBody))
                .andExpect(status().isCreated())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        String idStr = location.substring(location.lastIndexOf('/') + 1);
        UUID ordenId = UUID.fromString(idStr);

        // Validar planificacin
        OrdenProduccionJpaEntity entityPlanificada = repository.findByIdAndEmpresaId(ordenId, EMPRESA_ID).orElseThrow();
        assertThat(entityPlanificada.getEstado()).isEqualTo("PLANIFICADA");

        // 2. Iniciar Produccin
        mockMvc.perform(post("/production/orders/{id}/iniciar", ordenId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Validar inicio y evento
        OrdenProduccionJpaEntity entityEnProgreso = repository.findByIdAndEmpresaId(ordenId, EMPRESA_ID).orElseThrow();
        assertThat(entityEnProgreso.getEstado()).isEqualTo("EN_PROGRESO");

        long eventosIniciada = applicationEvents.stream(ProduccionIniciadaEvent.class).count();
        assertThat(eventosIniciada).isEqualTo(1);

        // 3. Completar Produccin
        mockMvc.perform(post("/production/orders/{id}/completar", ordenId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Validar completitud y evento
        OrdenProduccionJpaEntity entityCompletada = repository.findByIdAndEmpresaId(ordenId, EMPRESA_ID).orElseThrow();
        assertThat(entityCompletada.getEstado()).isEqualTo("COMPLETADA");

        long eventosCompletada = applicationEvents.stream(ProduccionCompletadaEvent.class).count();
        assertThat(eventosCompletada).isEqualTo(1);
    }
}
