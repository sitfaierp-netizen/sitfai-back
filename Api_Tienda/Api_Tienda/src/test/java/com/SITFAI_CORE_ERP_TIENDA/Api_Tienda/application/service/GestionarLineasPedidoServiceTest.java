package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.AgregarLineaPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.RemoverLineaPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ProductoId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Capa de Aplicación: GestionarLineasPedidoService (MCP-01, MT-01)")
class GestionarLineasPedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    private GestionarLineasPedidoService service;

    private final EmpresaId empresaId = EmpresaId.generar();
    private final ClienteId clienteId = ClienteId.generar();
    private final ProductoId productoId = ProductoId.generar();

    @BeforeEach
    void setUp() {
        service = new GestionarLineasPedidoService(pedidoRepository);
    }

    @Test
    @DisplayName("Debe agregar una línea a un pedido existente y recalcular el total")
    void debeAgregarLineaCorrectamente() {
        Pedido pedido = Pedido.iniciar(empresaId, clienteId);

        when(pedidoRepository.buscarPorId(pedido.getId(), empresaId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.guardar(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AgregarLineaPedidoCommand command = new AgregarLineaPedidoCommand(
                empresaId.valor(),
                pedido.getId().valor(),
                productoId.valor(),
                2,
                new BigDecimal("25.00"),
                "USD"
        );

        PedidoResponse response = service.agregarLinea(command);

        assertThat(response).isNotNull();
        assertThat(response.lineas()).hasSize(1);
        assertThat(response.total()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(response.lineas().get(0).productoId()).isEqualTo(productoId.valor());
        assertThat(response.lineas().get(0).cantidad()).isEqualTo(2);

        verify(pedidoRepository).guardar(pedido);
    }

    @Test
    @DisplayName("Debe remover una línea de un pedido existente")
    void debeRemoverLineaCorrectamente() {
        Pedido pedido = Pedido.iniciar(empresaId, clienteId);
        pedido.agregarItem(productoId, 2, Dinero.de(new BigDecimal("25.00"), "USD"));
        UUID lineaId = pedido.getLineas().get(0).getId();

        when(pedidoRepository.buscarPorId(pedido.getId(), empresaId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.guardar(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RemoverLineaPedidoCommand command = new RemoverLineaPedidoCommand(
                empresaId.valor(),
                pedido.getId().valor(),
                lineaId
        );

        PedidoResponse response = service.removerLinea(command);

        assertThat(response).isNotNull();
        assertThat(response.lineas()).isEmpty();
        assertThat(response.total()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(pedidoRepository).guardar(pedido);
    }

    @Test
    @DisplayName("Debe lanzar PedidoNoEncontradoException si el pedido no existe al agregar línea")
    void debeLanzarExcepcionAlAgregarLineaSiNoExiste() {
        PedidoId pedidoIdInexistente = PedidoId.generar();
        when(pedidoRepository.buscarPorId(pedidoIdInexistente, empresaId)).thenReturn(Optional.empty());

        AgregarLineaPedidoCommand command = new AgregarLineaPedidoCommand(
                empresaId.valor(),
                pedidoIdInexistente.valor(),
                productoId.valor(),
                1,
                new BigDecimal("10.00"),
                "USD"
        );

        assertThatThrownBy(() -> service.agregarLinea(command))
                .isInstanceOf(PedidoNoEncontradoException.class);

        verify(pedidoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("Debe lanzar PedidoInvalidoException si el pedido ya está confirmado y se intenta modificar")
    void debeRechazarModificacionEnPedidoConfirmado() {
        Pedido pedido = Pedido.iniciar(empresaId, clienteId);
        pedido.agregarItem(productoId, 1, Dinero.de(new BigDecimal("10.00"), "USD"));
        pedido.confirmar();

        when(pedidoRepository.buscarPorId(pedido.getId(), empresaId)).thenReturn(Optional.of(pedido));

        AgregarLineaPedidoCommand command = new AgregarLineaPedidoCommand(
                empresaId.valor(),
                pedido.getId().valor(),
                ProductoId.generar().valor(),
                1,
                new BigDecimal("20.00"),
                "USD"
        );

        assertThatThrownBy(() -> service.agregarLinea(command))
                .isInstanceOf(PedidoInvalidoException.class);

        verify(pedidoRepository, never()).guardar(any());
    }
}
