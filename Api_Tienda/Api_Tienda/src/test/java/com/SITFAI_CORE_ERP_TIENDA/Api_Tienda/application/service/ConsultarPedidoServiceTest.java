package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.PedidoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoNoEncontradoException;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Capa de Aplicación: ConsultarPedidoService (MCP-01, MT-01)")
class ConsultarPedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    private ConsultarPedidoService service;

    private final EmpresaId empresaId = EmpresaId.generar();
    private final ClienteId clienteId = ClienteId.generar();

    @BeforeEach
    void setUp() {
        service = new ConsultarPedidoService(pedidoRepository);
    }

    @Test
    @DisplayName("Debe consultar un pedido por ID y EmpresaId exitosamente")
    void debeConsultarPorIdExitosamente() {
        Pedido pedido = Pedido.iniciar(empresaId, clienteId);

        when(pedidoRepository.buscarPorId(pedido.getId(), empresaId)).thenReturn(Optional.of(pedido));

        PedidoResponse response = service.porId(pedido.getId().valor(), empresaId.valor());

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(pedido.getId().valor());
        assertThat(response.empresaId()).isEqualTo(empresaId.valor());
        verify(pedidoRepository).buscarPorId(pedido.getId(), empresaId);
    }

    @Test
    @DisplayName("Debe lanzar PedidoNoEncontradoException si el pedido no existe al consultar por ID")
    void debeLanzarExcepcionSiNoExistePorId() {
        PedidoId pedidoId = PedidoId.generar();
        when(pedidoRepository.buscarPorId(pedidoId, empresaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.porId(pedidoId.valor(), empresaId.valor()))
                .isInstanceOf(PedidoNoEncontradoException.class);
    }

    @Test
    @DisplayName("Debe consultar todos los pedidos de una empresa")
    void debeConsultarPorEmpresa() {
        Pedido pedido1 = Pedido.iniciar(empresaId, clienteId);
        Pedido pedido2 = Pedido.iniciar(empresaId, ClienteId.generar());

        when(pedidoRepository.buscarPorEmpresa(empresaId)).thenReturn(List.of(pedido1, pedido2));

        List<PedidoResponse> responses = service.porEmpresa(empresaId.valor());

        assertThat(responses).hasSize(2);
        verify(pedidoRepository).buscarPorEmpresa(empresaId);
    }
}
