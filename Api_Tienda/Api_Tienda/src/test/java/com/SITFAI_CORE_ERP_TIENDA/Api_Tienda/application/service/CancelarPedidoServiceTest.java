package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CancelarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.EstadoPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Capa de Aplicación: CancelarPedidoService (MCP-01, MT-01)")
class CancelarPedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    private CancelarPedidoService service;

    private final EmpresaId empresaId = EmpresaId.generar();
    private final ClienteId clienteId = ClienteId.generar();

    @BeforeEach
    void setUp() {
        service = new CancelarPedidoService(pedidoRepository);
    }

    @Test
    @DisplayName("Debe cancelar un pedido exitosamente y persistir el estado")
    void debeCancelarPedidoExitosamente() {
        Pedido pedido = Pedido.iniciar(empresaId, clienteId);

        when(pedidoRepository.buscarPorId(pedido.getId(), empresaId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.guardar(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CancelarPedidoCommand command = new CancelarPedidoCommand(
                empresaId.valor(),
                pedido.getId().valor(),
                "Cliente desistió de la compra"
        );

        PedidoResponse response = service.ejecutar(command);

        assertThat(response).isNotNull();
        assertThat(response.estado()).isEqualTo(EstadoPedido.CANCELADO.name());

        verify(pedidoRepository).guardar(pedido);
    }

    @Test
    @DisplayName("Debe lanzar PedidoNoEncontradoException si el pedido no existe")
    void debeLanzarExcepcionSiNoExiste() {
        PedidoId pedidoIdInexistente = PedidoId.generar();
        when(pedidoRepository.buscarPorId(pedidoIdInexistente, empresaId)).thenReturn(Optional.empty());

        CancelarPedidoCommand command = new CancelarPedidoCommand(
                empresaId.valor(),
                pedidoIdInexistente.valor(),
                "Motivo prueba"
        );

        assertThatThrownBy(() -> service.ejecutar(command))
                .isInstanceOf(PedidoNoEncontradoException.class);

        verify(pedidoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe lanzar PedidoInvalidoException si el pedido ya está cancelado")
    void debeRechazarCancelacionDuplicada() {
        Pedido pedido = Pedido.iniciar(empresaId, clienteId);
        pedido.cancelar("Primera cancelacion");

        when(pedidoRepository.buscarPorId(pedido.getId(), empresaId)).thenReturn(Optional.of(pedido));

        CancelarPedidoCommand command = new CancelarPedidoCommand(
                empresaId.valor(),
                pedido.getId().valor(),
                "Segunda cancelacion"
        );

        assertThatThrownBy(() -> service.ejecutar(command))
                .isInstanceOf(PedidoInvalidoException.class);

        verify(pedidoRepository, never()).guardar(any());
    }
}
