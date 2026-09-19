package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.CurrentActorProvider;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.EstadoTurno;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.*;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.TurnoCajaJpaAdapter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ApiTiendaApplication.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for this pure slice
@Import(TestcontainersConfiguration.class)
@Transactional
class DevolucionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TurnoCajaJpaAdapter turnoCajaRepository;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private TenantProviderPort tenantProviderPort;

    @org.springframework.test.context.bean.override.mockito.MockitoBean
    private CurrentActorProvider currentActorProvider;

    @Test
    void procesarDevolucion_ShouldReturn200AndPersistTransaction() throws Exception {
        // Arrange
        UUID turnoId = UUID.randomUUID();
        UUID empresaId = UUID.randomUUID();
        UUID ticketOriginalId = UUID.randomUUID();
        
        when(tenantProviderPort.getEmpresaIdAutenticada()).thenReturn(new EmpresaId(empresaId));
        when(currentActorProvider.getActorActual()).thenReturn("TEST_ACTOR");

        // Prepare Turno
        TurnoCaja turno = TurnoCaja.reconstituir(
                new TurnoId(turnoId),
                new EmpresaId(empresaId),
                new CajaId(UUID.randomUUID()),
                new SucursalId(UUID.randomUUID()),
                new UsuarioId(UUID.randomUUID()),
                EstadoTurno.ABIERTO,
                Dinero.de(new BigDecimal("100.00")),
                List.of(),
                null
        );
        turnoCajaRepository.guardar(turno);

        String payload = """
                {
                  "ticketOriginalId": "%s",
                  "montoDevuelto": 10.00,
                  "lineas": [
                    {
                      "productoId": "%s",
                      "cantidad": 1,
                      "precioUnitario": 10.00
                    }
                  ],
                  "lotesRevertidos": [
                    {
                      "productoId": "%s",
                      "codigoLote": "LOTE-123",
                      "cantidad": 1
                    }
                  ]
                }
                """.formatted(ticketOriginalId, UUID.randomUUID(), UUID.randomUUID());

        // Act & Assert
        mockMvc.perform(post("/api/v1/pos/turnos/{id}/devoluciones", turnoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk());

        // Verify DB state
        TurnoCaja updatedTurno = turnoCajaRepository.buscarPorId(new TurnoId(turnoId), new EmpresaId(empresaId)).orElseThrow();
        boolean hasDevolucionTx = updatedTurno.getTransacciones().stream()
                .anyMatch(tx -> tx.getReferencia().equals("DEV-" + ticketOriginalId));
        
        assert hasDevolucionTx;
    }
}
