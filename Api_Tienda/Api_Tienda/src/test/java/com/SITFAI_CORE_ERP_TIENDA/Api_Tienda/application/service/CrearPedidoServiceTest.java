package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CrearPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.EstadoPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Capa de Aplicación: CrearPedidoService (MCP-01, MT-01)")
class CrearPedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    private CrearPedidoService service;

    @BeforeEach
    void setUp() {
        service = new CrearPedidoService(pedidoRepository);
    }

    @Test
    @DisplayName("Debe inicializar un Pedido en estado CREADO y persistirlo con el tenant adecuado")
    void debeCrearPedidoExitosamente() {
        UUID empresaUuid = UUID.randomUUID();
        UUID clienteUuid = UUID.randomUUID();

        when(pedidoRepository.guardar(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CrearPedidoCommand command = new CrearPedidoCommand(empresaUuid, clienteUuid);
        PedidoResponse response = service.ejecutar(command);

        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.empresaId()).isEqualTo(empresaUuid);
        assertThat(response.clienteId()).isEqualTo(clienteUuid);
        assertThat(response.estado()).isEqualTo(EstadoPedido.CREADO.name());
        assertThat(response.total()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(response.lineas()).isEmpty();

        ArgumentCaptor<Pedido> pedidoCaptor = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).guardar(pedidoCaptor.capture());
        Pedido guardado = pedidoCaptor.getValue();
        assertThat(guardado.getEmpresaId().valor()).isEqualTo(empresaUuid);
        assertThat(guardado.getClienteId().valor()).isEqualTo(clienteUuid);
        assertThat(guardado.getEstado()).isEqualTo(EstadoPedido.CREADO);
    }

    @Test
    @DisplayName("Debe lanzar excepción si el command es null")
    void debeRechazarCommandNull() {
        assertThatThrownBy(() -> service.ejecutar(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("command no puede ser null");
    }
}
