package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.event.PedidoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.web.dto.CrearPedidoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity.PedidoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.repository.PedidoJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de IntegraciÃ³n: Ciclo Comercial del Agregado Pedido (Api_Tienda).
 * <p>
 * Simula:
 * 1. CreaciÃ³n de un pedido vÃ­a REST (POST /pedidos) con DTOs aislados.
 * 2. ConfirmaciÃ³n del pedido vÃ­a REST (PATCH /pedidos/{id}/confirmar).
 * 3. Persistencia en base de datos con clave de particiÃ³n empresa_id (MT-01).
 * 4. Despacho del evento PedidoConfirmadoEvent hacia el contexto de Spring (CoreografÃ­a con Bodega/Inventario).
 */
@Transactional
public class PedidoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PedidoJpaRepository pedidoRepository;


    private UUID empresaId;
    private UUID clienteId;
    private UUID productoId;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
        clienteId = UUID.randomUUID();
        productoId = UUID.randomUUID();
        when(apiTiendaTenantProviderPort.getEmpresaIdAutenticada()).thenReturn(EmpresaId.de(empresaId));
    }

    @Test
    @DisplayName("Ciclo completo del pedido: CreaciÃ³n vÃ­a REST, confirmaciÃ³n y despacho de evento")
    void testCrearYConfirmarPedido() throws Exception {
        // 1. Crear pedido vÃ­a REST POST /pedidos
        CrearPedidoWebRequest request = new CrearPedidoWebRequest(
                clienteId,
                List.of(
                        new CrearPedidoWebRequest.LineaWebRequest(productoId, 3, BigDecimal.valueOf(150.00))
                )
        );

        MvcResult createResult = mockMvc.perform(post("/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.total").value(450.00))
                .andExpect(jsonPath("$.lineas.length()").value(1))
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(createResult.getResponse().getContentAsString());
        String pedidoIdStr = jsonNode.get("id").asText();
        UUID pedidoId = UUID.fromString(pedidoIdStr);

        // Validar persistencia en BD viva (MySQL)
        Optional<PedidoJpaEntity> pedidoGuardado = pedidoRepository.findByIdAndEmpresaId(pedidoIdStr, empresaId.toString());
        assertThat(pedidoGuardado).isPresent();
        assertThat(pedidoGuardado.get().getEstado()).isEqualTo("PENDIENTE");
        assertThat(pedidoGuardado.get().getTotal()).isEqualByComparingTo(BigDecimal.valueOf(450.00));
        assertThat(pedidoGuardado.get().getLineas()).hasSize(1);



        // 2. Confirmar pedido vÃ­a REST PATCH /pedidos/{id}/confirmar
        mockMvc.perform(patch("/pedidos/" + pedidoId + "/confirmar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(pedidoIdStr))
                .andExpect(jsonPath("$.estado").value("CONFIRMADO"));

        // Validar estado persistido en BD
        Optional<PedidoJpaEntity> pedidoConfirmado = pedidoRepository.findByIdAndEmpresaId(pedidoIdStr, empresaId.toString());
        assertThat(pedidoConfirmado).isPresent();
        assertThat(pedidoConfirmado.get().getEstado()).isEqualTo("CONFIRMADO");

        // 3. Verificar que se despacha el evento de dominio hacia el contexto de Spring
        org.mockito.Mockito.verify(applicationEventPublisher, org.mockito.Mockito.times(1))
            .publishEvent(org.mockito.ArgumentMatchers.any(PedidoConfirmadoEvent.class));
    }

}

