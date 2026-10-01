package com.SITFAI_CORE_ERP_TIENDA.fulfillment;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test.AbstractIntegrationTest;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.LineaDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.OrdenDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.port.output.OrdenDespachoRepository;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.EstadoDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.event.DespachoCompletadoEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class OrdenDespachoIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrdenDespachoRepository repository;



    private UUID empresaId;
    private DespachoId despachoId;

    @BeforeEach
    void setUp() {
        empresaId = UUID.randomUUID();
        despachoId = DespachoId.generar();
        when(fulfillmentTenantProviderPort.getEmpresaIdAutenticada()).thenReturn(EmpresaId.de(empresaId));

        OrdenDespacho orden = OrdenDespacho.crear(
                EmpresaId.de(empresaId),
                despachoId,
                PedidoId.de(UUID.randomUUID()),
                BodegaId.de(UUID.randomUUID()),
                List.of(new LineaDespacho(ProductoId.de(UUID.randomUUID()), 10))
        );
        repository.guardar(EmpresaId.de(empresaId), orden);
    }

    @Test
    @DisplayName("Flujo completo de Fulfillment (picking -> empacar -> confirmar)")
    void flujoCompleto() throws Exception {
        // Iniciar Picking
        mockMvc.perform(post("/fulfillment/despachos/{id}/picking", despachoId.valor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EN_PICKING"));

        // Empacar
        mockMvc.perform(post("/fulfillment/despachos/{id}/empacar", despachoId.valor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("EMPACADO"));

        // Confirmar Despacho
        mockMvc.perform(post("/fulfillment/despachos/{id}/confirmar", despachoId.valor()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("DESPACHADO"));

        // Verificar base de datos
        OrdenDespacho ordenGuardada = repository.buscarPorId(EmpresaId.de(empresaId), despachoId).orElseThrow();
        assertThat(ordenGuardada.getEstado()).isEqualTo(EstadoDespacho.DESPACHADO);

        // Verificar evento de dominio
        org.junit.jupiter.api.Assertions.assertEquals(1, applicationEvents.stream(DespachoCompletadoEvent.class).count());
        DespachoCompletadoEvent event = applicationEvents.stream(DespachoCompletadoEvent.class)
            .findFirst()
            .orElseThrow();
        assertThat(event.empresaId().valor()).isEqualTo(empresaId);
        assertThat(event.despachoId()).isEqualTo(despachoId);
    }
}
