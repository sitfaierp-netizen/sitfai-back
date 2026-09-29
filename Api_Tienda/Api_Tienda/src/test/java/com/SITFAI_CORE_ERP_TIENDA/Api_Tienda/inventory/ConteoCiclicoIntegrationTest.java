package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.ConteoCiclico;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.DetalleConteo;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.event.DiscrepanciaInventarioDetectadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.port.ConteoCiclicoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.ConteoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.EstadoConteo;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.ProductoId;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Prueba de Integración: Conteo Cíclico y Auditoría de Inventario WMS.
 * <p>
 * Valida de extremo a extremo:
 * 1. Persistencia de agregados y cascada de detalles en base de datos.
 * 2. Registro de cantidades físicas vía API REST (PATCH /inventory/conteos/{id}/fisico).
 * 3. Finalización y cálculo analítico de discrepancias vía REST (POST /inventory/conteos/{id}/finalizar).
 * 4. Propagación de DiscrepanciaInventarioDetectadaEvent hacia el ApplicationEventPublisher.
 */
@Transactional
public class ConteoCiclicoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConteoCiclicoRepository conteoRepository;



    private UUID empresaId;
    private UUID bodegaId;
    private UUID productoAId;
    private UUID productoBId;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
        bodegaId = UUID.randomUUID();
        productoAId = UUID.randomUUID();
        productoBId = UUID.randomUUID();

        when(inventoryTenantProviderPort.getEmpresaIdAutenticada())
                .thenReturn(com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId.de(empresaId));
    }

    @Test
    @DisplayName("Ciclo Completo: Registro físico -> Finalización -> Detección de Discrepancia y Publicación de Evento")
    void debeEjecutarCicloCompletoDeConteoConDiscrepancias() throws Exception {
        // 1. Arrange: Inicializar un Conteo Cíclico en estado PLANIFICADO
        ConteoId conteoId = ConteoId.generar();
        List<DetalleConteo> detalles = List.of(
                new DetalleConteo(ProductoId.de(productoAId), 50),
                new DetalleConteo(ProductoId.de(productoBId), 100)
        );

        ConteoCiclico conteoInicial = ConteoCiclico.crear(
                conteoId,
                EmpresaId.de(empresaId),
                BodegaId.de(bodegaId),
                LocalDate.now().plusDays(1),
                detalles
        );
        conteoRepository.guardar(conteoInicial);

        // 2. Act: Registrar conteo físico para Producto A (coincidente con el teórico: 50)
        Map<String, Object> requestA = Map.of(
                "productoId", productoAId.toString(),
                "cantidadFisica", 50
        );

        mockMvc.perform(patch("/inventory/conteos/{id}/fisico", conteoId.valor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_EJECUCION"))
                .andExpect(jsonPath("$.totalLineas").value(2));

        // 3. Act: Registrar conteo físico para Producto B (con discrepancia: teórico 100, físico 92)
        Map<String, Object> requestB = Map.of(
                "productoId", productoBId.toString(),
                "cantidadFisica", 92
        );

        mockMvc.perform(patch("/inventory/conteos/{id}/fisico", conteoId.valor())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestB)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_EJECUCION"));

        // 4. Act: Finalizar conteo cíclico
        mockMvc.perform(post("/inventory/conteos/{id}/finalizar", conteoId.valor())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("CON_DISCREPANCIAS"))
                .andExpect(jsonPath("$.totalDiscrepancias").value(1));

        // 5. Assert: Verificar persistencia en base de datos
        Optional<ConteoCiclico> conteoFinalOpt = conteoRepository.buscarPorId(EmpresaId.de(empresaId), conteoId);
        assertThat(conteoFinalOpt).isPresent();

        ConteoCiclico conteoFinal = conteoFinalOpt.get();
        assertThat(conteoFinal.getEstado()).isEqualTo(EstadoConteo.CON_DISCREPANCIAS);

        DetalleConteo detalleB = conteoFinal.getDetalles().stream()
                .filter(d -> d.getProductoId().valor().equals(productoBId))
                .findFirst().orElseThrow();
        assertThat(detalleB.getCantidadTeorica()).isEqualTo(100);
        assertThat(detalleB.getCantidadFisica().valor()).isEqualTo(92);
        assertThat(detalleB.calcularDiferencia()).isEqualTo(-8);
        assertThat(detalleB.tieneDiscrepancia()).isTrue();

        // 6. Assert: Verificar propagación del evento de dominio hacia el ApplicationEventPublisher
        ArgumentCaptor<DiscrepanciaInventarioDetectadaEvent> captor =
                ArgumentCaptor.forClass(DiscrepanciaInventarioDetectadaEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        DiscrepanciaInventarioDetectadaEvent eventEmitido = captor.getValue();
        assertThat(eventEmitido.empresaId().valor()).isEqualTo(empresaId);
        assertThat(eventEmitido.conteoId()).isEqualTo(conteoId);
        assertThat(eventEmitido.bodegaId().valor()).isEqualTo(bodegaId);
        assertThat(eventEmitido.discrepancias()).hasSize(1);

        var discrepancia = eventEmitido.discrepancias().get(0);
        assertThat(discrepancia.productoId().valor()).isEqualTo(productoBId);
        assertThat(discrepancia.cantidadTeorica()).isEqualTo(100);
        assertThat(discrepancia.cantidadFisica()).isEqualTo(92);
        assertThat(discrepancia.diferencia()).isEqualTo(-8);
    }
}
