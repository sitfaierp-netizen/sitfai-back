package com.SITFAI_CORE_ERP_TIENDA.pos;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.TurnoCajaResponse;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.CurrentActorProvider;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.TurnoCerradoEvent;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.*;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.port.output.TurnoCajaRepository;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.AbrirTurnoWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.CerrarTurnoWebRequest;
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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de Integración para el Agregado TurnoCaja con infraestructura viva (Testcontainers MySQL 8.4).
 * <p>
 * Simula el ciclo completo:
 * 1. Apertura de turno vía endpoint REST (/api/v1/pos/turnos/abrir)
 * 2. Registro de transacciones (Ventas, Ingresos, Egresos)
 * 3. Cierre de turno vía endpoint REST (/api/v1/pos/turnos/{id}/cerrar)
 * 4. Verificación de persistencia del arqueo inmutable en MySQL
 * 5. Verificación de publicación del evento de dominio TurnoCerradoEvent
 */
@SpringBootTest(classes = ApiTiendaApplication.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({TestcontainersConfiguration.class, TurnoCajaIntegrationTest.TestTurnoCerradoEventListener.class})
@ActiveProfiles("test")
@Transactional
public class TurnoCajaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TurnoCajaRepository turnoCajaRepository;

    @Autowired
    private TestTurnoCerradoEventListener eventListener;

    @MockitoBean
    private TenantProviderPort tenantProviderPort;

    @MockitoBean
    private CurrentActorProvider currentActorProvider;

    private UUID tenantUuid;
    private UUID cajaUuid;
    private UUID cajeroUuid;

    @TestComponent
    public static class TestTurnoCerradoEventListener {
        private final List<TurnoCerradoEvent> events = new CopyOnWriteArrayList<>();

        @EventListener
        public void onTurnoCerrado(TurnoCerradoEvent event) {
            events.add(event);
        }

        public List<TurnoCerradoEvent> getEvents() {
            return events;
        }

        public void clear() {
            events.clear();
        }
    }

    @BeforeEach
    void setUp() {
        eventListener.clear();
        tenantUuid = UUID.randomUUID();
        cajaUuid = UUID.randomUUID();
        cajeroUuid = UUID.randomUUID();

        when(tenantProviderPort.getEmpresaIdAutenticada())
                .thenReturn(new com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId(tenantUuid));
        when(currentActorProvider.getActorActual()).thenReturn("cajero_test_user");
    }

    @Test
    @DisplayName("Debe abrir turno por API REST, operar transacciones, cerrarlo con declaración física y persistir arqueo inmutable en MySQL")
    void cicloCompleto_AperturaYCierreTurnoConArqueoInmutable() throws Exception {
        // 1. Apertura de Turno vía POST /api/v1/pos/turnos/abrir
        BigDecimal montoInicial = new BigDecimal("100.0000");
        AbrirTurnoWebRequest abrirRequest = new AbrirTurnoWebRequest(
                cajaUuid,
                UUID.randomUUID(),
                cajeroUuid,
                cajeroUuid,
                montoInicial
        );

        String abrirJson = mockMvc.perform(post("/api/v1/pos/turnos/abrir")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(abrirRequest)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TurnoCajaResponse abrirResponse = objectMapper.readValue(abrirJson, TurnoCajaResponse.class);
        assertNotNull(abrirResponse.id());
        assertEquals("ABIERTO", abrirResponse.estado());
        assertEquals(tenantUuid, abrirResponse.empresaId());
        assertEquals(cajaUuid, abrirResponse.cajaId());

        UUID turnoId = abrirResponse.id();

        // 2. Registrar movimientos directamente en el agregado
        var empresaIdDomain = EmpresaId.of(tenantUuid);
        TurnoCaja turnoCargado = turnoCajaRepository.buscarPorId(TurnoId.of(turnoId), empresaIdDomain)
                .orElseThrow();
        assertEquals(EstadoTurno.ABIERTO, turnoCargado.getEstado());

        turnoCargado.registrarTransaccion(TipoTransaccionCaja.VENTA, Dinero.de("250.5000"), "TICKET-1001");
        turnoCargado.registrarTransaccion(TipoTransaccionCaja.INGRESO, Dinero.de("50.0000"), "ING-1001");
        turnoCargado.registrarTransaccion(TipoTransaccionCaja.EGRESO, Dinero.de("20.0000"), "EGR-1001");
        turnoCajaRepository.guardar(turnoCargado, empresaIdDomain);

        // Balance Teórico = 100 + 250.50 + 50 - 20 = 380.50
        // Declaramos monto físico con sobrante de $5: 385.50
        BigDecimal montoFisicoDeclarado = new BigDecimal("385.5000");
        CerrarTurnoWebRequest cerrarRequest = new CerrarTurnoWebRequest(montoFisicoDeclarado, montoFisicoDeclarado);

        // 3. Cierre de Turno vía POST /api/v1/pos/turnos/{id}/cerrar
        String cerrarJson = mockMvc.perform(post("/api/v1/pos/turnos/" + turnoId + "/cerrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cerrarRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TurnoCajaResponse cerrarResponse = objectMapper.readValue(cerrarJson, TurnoCajaResponse.class);
        assertEquals("CERRADO", cerrarResponse.estado());

        // 4. Assert: Verificar persistencia del Arqueo Inmutable en base de datos
        TurnoCaja turnoFinal = turnoCajaRepository.buscarPorId(TurnoId.of(turnoId), empresaIdDomain)
                .orElseThrow();
        assertEquals(EstadoTurno.CERRADO, turnoFinal.getEstado());
        assertEquals(Dinero.de("385.5000"), turnoFinal.getMontoCierre());
        assertNotNull(turnoFinal.getArqueo());

        var arqueo = turnoFinal.getArqueo();
        assertEquals(Dinero.de("100.0000"), arqueo.montoApertura());
        assertEquals(Dinero.de("250.5000"), arqueo.totalVentas());
        assertEquals(Dinero.de("50.0000"), arqueo.totalIngresos());
        assertEquals(Dinero.de("20.0000"), arqueo.totalEgresos());
        assertEquals(Dinero.de("380.5000"), arqueo.totalTeoricoEsperado());
        assertEquals(Dinero.de("385.5000"), arqueo.montoFisicoDeclarado());
        assertEquals(Dinero.de("5.0000"), arqueo.descuadre());
        assertTrue(arqueo.tieneSobrante());

        // 5. Assert: Verificar disparo del evento de dominio TurnoCerradoEvent
        assertEquals(1, eventListener.getEvents().size());
        TurnoCerradoEvent event = eventListener.getEvents().get(0);
        assertEquals(turnoId, event.turnoId().value());
        assertEquals(tenantUuid, event.empresaId().value());
        assertEquals(Dinero.de("380.5000"), event.totalTeoricoEsperado());
        assertEquals(Dinero.de("385.5000"), event.montoFisicoDeclarado());
        assertEquals(Dinero.de("5.0000"), event.descuadre());
    }
}
