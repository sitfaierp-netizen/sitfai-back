package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.ConfirmarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.EstadoPedido;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Aplicación: ConfirmarPedidoService")
class ConfirmarPedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private PedidoEventPublisher eventPublisher;

    private ConfirmarPedidoService service;

    private final EmpresaId empresaId = EmpresaId.generar();
    private final ClienteId clienteId = ClienteId.generar();
    private final ProductoId productoId = ProductoId.generar();

    @BeforeEach
    void setUp() {
        service = new ConfirmarPedidoService(pedidoRepository, eventPublisher);
    }

    @Test
    @DisplayName("Debe confirmar pedido con éxito, persistir y publicar eventos de dominio")
    void debeConfirmarPedidoExitosamente() {
        Pedido pedido = Pedido.crear(empresaId, clienteId);
        pedido.agregarLinea(productoId, 2, Dinero.de(15.0));

        when(pedidoRepository.buscarPorId(pedido.getId(), empresaId)).thenReturn(Optional.of(pedido));
        when(pedidoRepository.guardar(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ConfirmarPedidoCommand command = new ConfirmarPedidoCommand(empresaId.valor(), pedido.getId().valor());
        PedidoResponse response = service.ejecutar(command);

        assertNotNull(response);
        assertEquals(EstadoPedido.CONFIRMADO.name(), response.estado());
        assertEquals(new BigDecimal("30.00"), response.total());

        verify(pedidoRepository).guardar(pedido);
        verify(eventPublisher).publicarTodos(any());
    }

    @Test
    @DisplayName("Debe lanzar PedidoNoEncontradoException si el pedido no existe o no es del tenant")
    void debeLanzarExcepcionSiPedidoNoExiste() {
        UUID pedidoUuid = UUID.randomUUID();
        when(pedidoRepository.buscarPorId(PedidoId.de(pedidoUuid), empresaId)).thenReturn(Optional.empty());

        ConfirmarPedidoCommand command = new ConfirmarPedidoCommand(empresaId.valor(), pedidoUuid);

        assertThrows(PedidoNoEncontradoException.class, () -> service.ejecutar(command));
        verify(pedidoRepository, never()).guardar(any());
        verify(eventPublisher, never()).publicarTodos(any());
    }

    @Test
    @DisplayName("Debe propagar PedidoInvalidoException desde el dominio si el pedido no tiene líneas")
    void debePropagarExcepcionSiPedidoVacio() {
        Pedido pedidoVacio = Pedido.crear(empresaId, clienteId);

        when(pedidoRepository.buscarPorId(pedidoVacio.getId(), empresaId)).thenReturn(Optional.of(pedidoVacio));

        ConfirmarPedidoCommand command = new ConfirmarPedidoCommand(empresaId.valor(), pedidoVacio.getId().valor());

        assertThrows(PedidoInvalidoException.class, () -> service.ejecutar(command));
        verify(pedidoRepository, never()).guardar(any());
        verify(eventPublisher, never()).publicarTodos(any());
    }
}
