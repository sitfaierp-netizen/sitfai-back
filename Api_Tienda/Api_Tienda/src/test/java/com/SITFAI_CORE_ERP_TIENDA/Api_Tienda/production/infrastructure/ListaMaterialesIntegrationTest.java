package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;

import org.springframework.context.annotation.Import;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.event.RecetaProduccionAprobadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.persistence.ListaMaterialesSpringDataRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.infrastructure.adapter.out.persistence.entity.ListaMaterialesJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class ListaMaterialesIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ListaMaterialesSpringDataRepository repository;

    @Autowired
    private ApplicationEvents applicationEvents;



    private final UUID EMPRESA_ID = UUID.randomUUID();
    private final UUID PRODUCTO_FINAL_ID = UUID.randomUUID();
    private final UUID INSUMO_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        Mockito.when(productionTenantProviderPort.getEmpresaIdAutenticada())
               .thenReturn(new EmpresaId(EMPRESA_ID));
    }

    @Test
    void debeCrearAgregarComponenteYAprobarBOM() throws Exception {
        // 1. Crear Borrador
        String createBody = String.format("{\"productoFinalId\":\"%s\"}", PRODUCTO_FINAL_ID);
        MvcResult result = mockMvc.perform(post("/production/bom")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated())
                .andReturn();

        String location = result.getResponse().getHeader("Location");
        assertThat(location).isNotNull();
        String idStr = location.substring(location.lastIndexOf('/') + 1);
        UUID recetaId = UUID.fromString(idStr);

        // Validar creacin inicial
        ListaMaterialesJpaEntity entityBorrador = repository.findByIdAndEmpresaId(recetaId, EMPRESA_ID).orElseThrow();
        assertThat(entityBorrador.getEstado()).isEqualTo("BORRADOR");
        assertThat(entityBorrador.getComponentes()).isEmpty();

        // 2. Agregar Componente
        String componentBody = String.format("{\"insumoId\":\"%s\", \"cantidad\": 2.5000}", INSUMO_ID);
        mockMvc.perform(patch("/production/bom/{id}/componentes", recetaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(componentBody))
                .andExpect(status().isOk());

        // Validar componente
        ListaMaterialesJpaEntity entityConComponente = repository.findByIdAndEmpresaId(recetaId, EMPRESA_ID).orElseThrow();
        assertThat(entityConComponente.getComponentes()).hasSize(1);
        assertThat(entityConComponente.getComponentes().get(0).getCantidad()).isEqualByComparingTo("2.5");

        // 3. Aprobar Receta
        mockMvc.perform(post("/production/bom/{id}/aprobar", recetaId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Validar aprobacin
        ListaMaterialesJpaEntity entityAprobada = repository.findByIdAndEmpresaId(recetaId, EMPRESA_ID).orElseThrow();
        assertThat(entityAprobada.getEstado()).isEqualTo("APROBADA");

        // 4. Verificar Evento Publicado
        long eventCount = applicationEvents.stream(RecetaProduccionAprobadaEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
    }
}
