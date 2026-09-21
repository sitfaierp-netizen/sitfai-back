package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.CurrentActorProvider;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.CrearPedidoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.repository.PedidoJpaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
public class PedidoIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PedidoJpaRepository pedidoRepository;

    @MockBean
    private TenantProviderPort tenantProviderPort;

    @MockBean
    private CurrentActorProvider currentActorProvider;

    @Test
    void testCrearPedido() throws Exception {
        UUID empresaId = UUID.randomUUID();
        UUID clienteId = UUID.randomUUID();
        UUID productoId = UUID.randomUUID();

        when(tenantProviderPort.getEmpresaIdAutenticada()).thenReturn(EmpresaId.de(empresaId));
        when(currentActorProvider.getActorActual()).thenReturn("test-actor");

        CrearPedidoWebRequest request = new CrearPedidoWebRequest(
                clienteId,
                List.of(
                        new CrearPedidoWebRequest.LineaWebRequest(productoId, 2, BigDecimal.valueOf(100.00))
                )
        );

        mockMvc.perform(post("/api/v1/api-tienda/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("RESERVANDO_STOCK"))
                .andExpect(jsonPath("$.lineas.length()").value(1));

        assertThat(pedidoRepository.findByEmpresaId(empresaId.toString())).hasSize(1);
    }
}
