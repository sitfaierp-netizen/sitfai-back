package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;

import org.springframework.context.annotation.Import;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.event.MercanciaRecibidaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.RecepcionMercanciaSpringDataRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.LineaRecepcionJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity.RecepcionMercanciaJpaEntity;
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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@org.springframework.transaction.annotation.Transactional
public class RecepcionMercanciaIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RecepcionMercanciaSpringDataRepository repository;

    @Autowired
    private ApplicationEvents applicationEvents;



    private final UUID EMPRESA_ID = UUID.randomUUID();
    private final UUID RECEPCION_ID = UUID.randomUUID();
    private final UUID PRODUCTO_ID = UUID.randomUUID();
    private final UUID BODEGA_ID = UUID.randomUUID();
    private final UUID ORDEN_COMPRA_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        Mockito.when(inventoryTenantProviderPort.getEmpresaIdAutenticada())
               .thenReturn(new com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId(EMPRESA_ID));

        // Crear una recepción planificada directamente en BD para la prueba
        RecepcionMercanciaJpaEntity recepcion = new RecepcionMercanciaJpaEntity();
        recepcion.setId(RECEPCION_ID);
        recepcion.setEmpresaId(EMPRESA_ID);
        recepcion.setOrdenCompraId(ORDEN_COMPRA_ID);
        recepcion.setBodegaId(BODEGA_ID);
        recepcion.setEstado("PLANIFICADA");

        LineaRecepcionJpaEntity linea = new LineaRecepcionJpaEntity();
        linea.setId(UUID.randomUUID());
        linea.setProductoId(PRODUCTO_ID);
        linea.setCantidadEsperada(10);
        linea.setCantidadRecibida(0);
        linea.setRecepcion(recepcion);

        recepcion.getLineas().add(linea);
        repository.save(recepcion);
    }

    @Test
    void debeRegistrarProductoYCompletarRecepcion() throws Exception {
        // 1. Registrar producto parcialmente (PATCH)
        String requestBody = String.format("{\"productoId\":\"%s\", \"cantidad\": 10}", PRODUCTO_ID);
        mockMvc.perform(patch("/inventory/recepciones/{id}/productos", RECEPCION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        // Verificar que el estado cambió a EN_PROCESO en BD y se guardó la cantidad
        RecepcionMercanciaJpaEntity recepcionEnProceso = repository.findByIdAndEmpresaId(RECEPCION_ID, EMPRESA_ID).orElseThrow();
        assertThat(recepcionEnProceso.getEstado()).isEqualTo("EN_PROCESO");
        assertThat(recepcionEnProceso.getLineas().get(0).getCantidadRecibida()).isEqualTo(10);

        // 2. Completar recepción (POST)
        mockMvc.perform(post("/inventory/recepciones/{id}/completar", RECEPCION_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Verificar que el estado cambió a COMPLETADA
        RecepcionMercanciaJpaEntity recepcionCompletada = repository.findByIdAndEmpresaId(RECEPCION_ID, EMPRESA_ID).orElseThrow();
        assertThat(recepcionCompletada.getEstado()).isEqualTo("COMPLETADA");

        // 3. Verificar publicación de evento asíncrono
        long eventCount = applicationEvents.stream(MercanciaRecibidaEvent.class).count();
        assertThat(eventCount).isEqualTo(1);
    }
}
