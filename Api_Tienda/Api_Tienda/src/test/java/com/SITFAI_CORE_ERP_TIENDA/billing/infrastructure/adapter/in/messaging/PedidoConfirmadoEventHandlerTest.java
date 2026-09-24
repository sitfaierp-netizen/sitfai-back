package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.LineaPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.FacturaResponse;
import com.SITFAI_CORE_ERP_TIENDA.billing.application.port.input.EmitirFacturaUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoConfirmadoEventHandlerTest {

    @Mock
    private EmitirFacturaUseCase emitirFacturaUseCase;

    private PedidoConfirmadoEventHandler handler;

    @BeforeEach
    void setUp() {
        handler = new PedidoConfirmadoEventHandler(emitirFacturaUseCase);
    }

    @Test
    @DisplayName("Debe procesar PedidoConfirmadoEvent y delegar a EmitirFacturaUseCase con los datos correctos")
    void debeProcesarPedidoConfirmadoEventCorrectamente() {
        // Arrange
        UUID empresaIdRaw = UUID.randomUUID();
        UUID clienteIdRaw = UUID.randomUUID();
        UUID pedidoIdRaw = UUID.randomUUID();
        UUID productoIdRaw = UUID.randomUUID();

        LineaPedido linea = LineaPedido.crear(
                ProductoId.de(productoIdRaw),
                3,
                Dinero.de(new BigDecimal("25000.00"), "COP")
        );

        PedidoConfirmadoEvent event = PedidoConfirmadoEvent.of(
                PedidoId.de(pedidoIdRaw),
                EmpresaId.de(empresaIdRaw),
                ClienteId.de(clienteIdRaw),
                Dinero.de(new BigDecimal("75000.00"), "COP"),
                List.of(linea)
        );

        when(emitirFacturaUseCase.emitirFactura(any(EmitirFacturaCommand.class)))
                .thenReturn(new FacturaResponse(
                        UUID.randomUUID().toString(),
                        empresaIdRaw.toString(),
                        clienteIdRaw.toString(),
                        pedidoIdRaw.toString(),
                        "CONSUMIDOR_FINAL",
                        new BigDecimal("75000.00"),
                        new BigDecimal("14250.00"),
                        new BigDecimal("89250.00"),
                        "EMITIDA",
                        List.of()
                ));

        // Act
        handler.onPedidoConfirmado(event);

        // Assert
        ArgumentCaptor<EmitirFacturaCommand> captor = ArgumentCaptor.forClass(EmitirFacturaCommand.class);
        verify(emitirFacturaUseCase).emitirFactura(captor.capture());

        EmitirFacturaCommand captured = captor.getValue();
        assertThat(captured.empresaId()).isEqualTo(empresaIdRaw);
        assertThat(captured.clienteId()).isEqualTo(clienteIdRaw);
        assertThat(captured.pedidoId()).isEqualTo(pedidoIdRaw);
        assertThat(captured.tipoOrigen()).isEqualTo("ECOMMERCE");
        assertThat(captured.documentoFuenteId()).isEqualTo(pedidoIdRaw);
        assertThat(captured.lineas()).hasSize(1);
        assertThat(captured.lineas().get(0).cantidad()).isEqualByComparingTo(new BigDecimal("3"));
        assertThat(captured.lineas().get(0).precioUnitario()).isEqualByComparingTo(new BigDecimal("25000.00"));
    }
}
