package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import tools.jackson.databind.ObjectMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.DescontarStockUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RegistrarIngresoStockUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.DescontarStockRequest;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.RegistrarIngresoStockRequest;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security.GlobalSecurityConfig;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security.KeycloakJwtAuthenticationConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InventoryController.class)
@DisplayName("Pruebas de Hardening REST para InventoryController")
class InventoryControllerTest {

    // Al definir @SpringBootConfiguration localmente, evitamos que @WebMvcTest
    // escanee la clase principal ApiTiendaApplication y cargue @EnableJpaRepositories.
    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import({InventoryController.class, GlobalSecurityConfig.class, KeycloakJwtAuthenticationConverter.class, ValidationAutoConfiguration.class})
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RegistrarIngresoStockUseCase registrarIngresoStockUseCase;

    @MockitoBean
    private DescontarStockUseCase descontarStockUseCase;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    private Jwt createMockJwt(String role) {
        return Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", UUID.randomUUID().toString())
                .claim("empresa_id", UUID.randomUUID().toString())
                .claim("realm_access", Map.of("roles", List.of(role)))
                .build();
    }

    @Test
    @DisplayName("Debe registrar ingreso exitosamente con rol BODEGA_OPERATOR")
    void registrarIngreso_ConRolBodegaOperator_DebeRetornarOk() throws Exception {
        when(jwtDecoder.decode(anyString())).thenReturn(createMockJwt("BODEGA_OPERATOR"));

        UUID bodegaId = UUID.randomUUID();
        RegistrarIngresoStockRequest request = new RegistrarIngresoStockRequest(
                UUID.randomUUID(),
                BigDecimal.valueOf(10.0),
                "LOTE-A",
                Instant.now().plusSeconds(86400),
                "COMPRA",
                "OC-001"
        );

        mockMvc.perform(post("/inventory/bodegas/{bodegaId}/ingresos", bodegaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(registrarIngresoStockUseCase, times(1)).registrarIngreso(any());
    }

    @Test
    @DisplayName("Debe descontar stock exitosamente con rol EMPRESA_ADMIN")
    void descontarStock_ConRolEmpresaAdmin_DebeRetornarOk() throws Exception {
        when(jwtDecoder.decode(anyString())).thenReturn(createMockJwt("EMPRESA_ADMIN"));

        UUID bodegaId = UUID.randomUUID();
        DescontarStockRequest request = new DescontarStockRequest(
                UUID.randomUUID(),
                BigDecimal.valueOf(5.0),
                "VENTA",
                "VEN-001"
        );

        mockMvc.perform(post("/inventory/bodegas/{bodegaId}/egresos", bodegaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(descontarStockUseCase, times(1)).descontarStock(any());
    }

    @Test
    @DisplayName("Debe rechazar con HTTP 403 Forbidden a usuario sin roles autorizados")
    void registrarIngreso_SinRolAutorizado_DebeRetornarForbidden() throws Exception {
        when(jwtDecoder.decode(anyString())).thenReturn(createMockJwt("USUARIO_SIN_PERMISO"));

        UUID bodegaId = UUID.randomUUID();
        RegistrarIngresoStockRequest request = new RegistrarIngresoStockRequest(
                UUID.randomUUID(),
                BigDecimal.valueOf(10.0),
                "LOTE-A",
                Instant.now().plusSeconds(86400),
                "COMPRA",
                "OC-001"
        );

        mockMvc.perform(post("/inventory/bodegas/{bodegaId}/ingresos", bodegaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        verify(registrarIngresoStockUseCase, times(0)).registrarIngreso(any());
    }

    @Test
    @DisplayName("Debe rechazar con HTTP 401 Unauthorized cuando no hay credenciales")
    void registrarIngreso_SinCredenciales_DebeRetornarUnauthorized() throws Exception {
        UUID bodegaId = UUID.randomUUID();
        RegistrarIngresoStockRequest request = new RegistrarIngresoStockRequest(
                UUID.randomUUID(),
                BigDecimal.valueOf(10.0),
                "LOTE-A",
                Instant.now().plusSeconds(86400),
                "COMPRA",
                "OC-001"
        );

        mockMvc.perform(post("/inventory/bodegas/{bodegaId}/ingresos", bodegaId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Debe rechazar con HTTP 400 Bad Request si la cantidad es menor a cero")
    void registrarIngreso_CantidadInvalida_DebeRetornarBadRequest() throws Exception {
        when(jwtDecoder.decode(anyString())).thenReturn(createMockJwt("BODEGA_OPERATOR"));

        UUID bodegaId = UUID.randomUUID();
        RegistrarIngresoStockRequest request = new RegistrarIngresoStockRequest(
                UUID.randomUUID(),
                BigDecimal.valueOf(-1.0),
                "LOTE-A",
                Instant.now().plusSeconds(86400),
                "COMPRA",
                "OC-001"
        );

        mockMvc.perform(post("/inventory/bodegas/{bodegaId}/ingresos", bodegaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(registrarIngresoStockUseCase, times(0)).registrarIngreso(any());
    }

    @Test
    @DisplayName("Debe rechazar con HTTP 400 Bad Request si faltan campos obligatorios")
    void registrarIngreso_SinProductoId_DebeRetornarBadRequest() throws Exception {
        when(jwtDecoder.decode(anyString())).thenReturn(createMockJwt("BODEGA_OPERATOR"));

        UUID bodegaId = UUID.randomUUID();
        RegistrarIngresoStockRequest request = new RegistrarIngresoStockRequest(
                null, // Inválido por @NotNull
                BigDecimal.valueOf(10.0),
                "LOTE-A",
                Instant.now().plusSeconds(86400),
                "COMPRA",
                "OC-001"
        );

        mockMvc.perform(post("/inventory/bodegas/{bodegaId}/ingresos", bodegaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(registrarIngresoStockUseCase, times(0)).registrarIngreso(any());
    }

    @Test
    @DisplayName("Debe rechazar un payload que intente inyectar empresaId o sea malformado")
    void descontarStock_PayloadInvalido_DebeRetornarBadRequest() throws Exception {
        when(jwtDecoder.decode(anyString())).thenReturn(createMockJwt("BODEGA_OPERATOR"));

        UUID bodegaId = UUID.randomUUID();
        // Faltan campos obligatorios en este JSON, lo que debe lanzar 400 Bad Request
        String payloadMaligno = "{\"empresaId\":\"e024227e-8bf0-41da-a7a5-d8edbf383a17\",\"cantidad\":5.0}";

        mockMvc.perform(post("/inventory/bodegas/{bodegaId}/egresos", bodegaId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payloadMaligno))
                .andExpect(status().isBadRequest());

        verify(descontarStockUseCase, times(0)).descontarStock(any());
    }
}
