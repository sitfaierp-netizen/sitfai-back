package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoCanceladoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.EstadoPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.ItemPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ProductoId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Dominio: Agregado Pedido (Api_Tienda)")
class PedidoTest {

    private final EmpresaId empresaId = EmpresaId.generar();
    private final ClienteId clienteId = ClienteId.generar();
    private final ProductoId prod1 = ProductoId.generar();
    private final ProductoId prod2 = ProductoId.generar();

    @Nested
    @DisplayName("Inicio e Invariantes de Creación")
    class InicioTests {

        @Test
        @DisplayName("Debe iniciar un pedido nuevo en estado CREADO con lista vacía")
        void debeIniciarPedidoNuevo() {
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);

            assertNotNull(pedido.getId());
            assertEquals(empresaId, pedido.getEmpresaId());
            assertEquals(clienteId, pedido.getClienteId());
            assertEquals(EstadoPedido.CREADO, pedido.getEstado());
            assertTrue(pedido.getItems().isEmpty());
            assertTrue(pedido.getLineas().isEmpty());
            assertEquals(BigDecimal.ZERO.setScale(2), pedido.calcularTotal().monto());
        }

        @Test
        @DisplayName("El alias crear(...) debe iniciar el pedido en estado CREADO")
        void debeCrearPedidoConAlias() {
            Pedido pedido = Pedido.crear(empresaId, clienteId);
            assertEquals(EstadoPedido.CREADO, pedido.getEstado());
        }
    }

    @Nested
    @DisplayName("Gestión de Ítems e Invariantes de Modificación")
    class GestionItemsTests {

        @Test
        @DisplayName("Debe agregar ítems con Cantidad VO y calcular total correctamente")
        void debeAgregarItemsYCalcularTotal() {
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);

            pedido.agregarItem(prod1, Cantidad.de(2), Dinero.de(10.0)); // Subtotal: 20.00
            pedido.agregarItem(prod2, Cantidad.de(1), Dinero.de(15.50)); // Subtotal: 15.50

            assertEquals(2, pedido.getItems().size());
            assertEquals(new BigDecimal("35.50"), pedido.calcularTotal().monto());
        }

        @Test
        @DisplayName("Debe incrementar cantidad si se agrega el mismo producto con el mismo precio")
        void debeIncrementarCantidadProductoDuplicado() {
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);

            pedido.agregarItem(prod1, 2, Dinero.de(10.0));
            pedido.agregarItem(prod1, 3, Dinero.de(10.0));

            assertEquals(1, pedido.getItems().size());
            assertEquals(5, pedido.getItems().get(0).getCantidad().valor());
            assertEquals(new BigDecimal("50.00"), pedido.calcularTotal().monto());
        }

        @Test
        @DisplayName("Debe remover un ítem por su ID")
        void debeRemoverItem() {
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.agregarItem(prod1, 2, Dinero.de(10.0));
            pedido.agregarItem(prod2, 1, Dinero.de(15.0));

            ItemPedido primerItem = pedido.getItems().get(0);
            pedido.removerItem(primerItem.getId());

            assertEquals(1, pedido.getItems().size());
            assertEquals(prod2, pedido.getItems().get(0).getProductoId());
        }
    }

    @Nested
    @DisplayName("Confirmación y Eventos de Dominio")
    class ConfirmacionTests {

        @Test
        @DisplayName("Invariante: No debe permitir confirmar un pedido sin ítems")
        void noDebeConfirmarPedidoVacio() {
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);

            PedidoInvalidoException ex = assertThrows(PedidoInvalidoException.class, pedido::confirmar);
            assertTrue(ex.getMessage().contains("líneas está vacía") || ex.getMessage().contains("vacía"));
        }

        @Test
        @DisplayName("Debe confirmar pedido, cambiar estado a CONFIRMADO y emitir PedidoConfirmadoEvent")
        void debeConfirmarPedidoConLineasYEmitirEvento() {
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.agregarItem(prod1, 2, Dinero.de(25.0));

            pedido.confirmar();

            assertEquals(EstadoPedido.CONFIRMADO, pedido.getEstado());

            List<DomainEvent> eventos = pedido.drainDomainEvents();
            assertEquals(1, eventos.size());
            assertTrue(eventos.get(0) instanceof PedidoConfirmadoEvent);

            PedidoConfirmadoEvent evento = (PedidoConfirmadoEvent) eventos.get(0);
            assertEquals(pedido.getId(), evento.pedidoId());
            assertEquals(empresaId, evento.empresaId());
            assertEquals(clienteId, evento.clienteId());
            assertEquals(new BigDecimal("50.00"), evento.total().monto());
            assertEquals(1, evento.lineas().size());

            // Verificar que los eventos fueron drenados
            assertTrue(pedido.getDomainEvents().isEmpty());
        }

        @Test
        @DisplayName("Invariante: No debe permitir modificar un pedido ya confirmado")
        void noDebeModificarPedidoConfirmado() {
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.agregarItem(prod1, 1, Dinero.de(10.0));
            pedido.confirmar();

            assertThrows(PedidoInvalidoException.class, () ->
                    pedido.agregarItem(prod2, 1, Dinero.de(5.0)));
            assertThrows(PedidoInvalidoException.class, pedido::confirmar);
        }

        @Test
        @DisplayName("SAGA Happy Path: Debe confirmar reserva si estado es RESERVANDO_STOCK")
        void debeConfirmarReservaCuandoEstadoEsReservandoStock() {
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.agregarItem(prod1, 2, Dinero.de(30.0));
            pedido.solicitarReserva();
            pedido.drainDomainEvents();

            pedido.confirmarReserva();

            assertEquals(EstadoPedido.CONFIRMADO, pedido.getEstado());

            List<DomainEvent> eventos = pedido.drainDomainEvents();
            assertThat(eventos).hasSize(1);
            assertThat(eventos.get(0)).isInstanceOf(PedidoConfirmadoEvent.class);

            PedidoConfirmadoEvent evento = (PedidoConfirmadoEvent) eventos.get(0);
            assertEquals(pedido.getId(), evento.pedidoId());
            assertEquals(empresaId, evento.empresaId());
            assertEquals(new BigDecimal("60.00"), evento.total().monto());
        }

        @Test
        @DisplayName("SAGA Happy Path: Debe fallar confirmarReserva si estado no es RESERVANDO_STOCK")
        void debeFallarConfirmarReservaSiNoEstaEnReservandoStock() {
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.agregarItem(prod1, 1, Dinero.de(10.0));

            // Estado CREADO, no RESERVANDO_STOCK
            assertThrows(PedidoInvalidoException.class, pedido::confirmarReserva);
        }
    }

    @Nested
    @DisplayName("Cancelación de Pedido")
    class CancelacionTests {

        @Test
        @DisplayName("Debe cancelar un pedido en estado CREADO")
        void debeCancelarPedido() {
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.cancelar("Cliente solicitó cancelación");

            assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
        }

        @Test
        @DisplayName("Debe fallar al intentar cancelar un pedido ya cancelado")
        void debeFallarCancelarPedidoYaCancelado() {
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.cancelar("Primera cancelacion");

            assertThrows(PedidoInvalidoException.class, () -> pedido.cancelar("Segunda cancelacion"));
        }

        @Test
        @DisplayName("SAGA: Debe cancelar por falta de stock si el estado es RESERVANDO_STOCK")
        void debeCancelarPorFaltaDeStockSiEstadoEsReservandoStock() {
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.agregarItem(prod1, 2, Dinero.de(10.0));
            pedido.solicitarReserva(); // Cambia a RESERVANDO_STOCK

            pedido.cancelarPorFaltaDeStock("Stock insuficiente en bodega");

            assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());

            List<DomainEvent> eventos = pedido.drainDomainEvents();
            // Expected 2 events: PedidoCreadoEvent (from solicitarReserva) and PedidoCanceladoEvent
            assertEquals(2, eventos.size());
            assertTrue(eventos.get(1) instanceof com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoCanceladoEvent);

            com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoCanceladoEvent evento = (com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoCanceladoEvent) eventos.get(1);
            assertEquals(pedido.getId(), evento.pedidoId());
            assertEquals(empresaId, evento.empresaId());
            assertEquals(clienteId, evento.clienteId());
            assertEquals("Stock insuficiente en bodega", evento.motivo());
        }

        @Test
        @DisplayName("SAGA: Debe fallar al intentar cancelar por falta de stock si no está en RESERVANDO_STOCK")
        void debeFallarCancelarPorFaltaDeStockSiNoEstaEnReservandoStock() {
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.agregarItem(prod1, 2, Dinero.de(10.0));

            // Estado es CREADO, no RESERVANDO_STOCK
            PedidoInvalidoException ex = assertThrows(PedidoInvalidoException.class, () ->
                    pedido.cancelarPorFaltaDeStock("Stock insuficiente"));
            
            assertTrue(ex.getMessage().contains("no está en estado RESERVANDO_STOCK"));
        }

        @Test
        @DisplayName("MT-01: Invariante de Aislamiento Tenant en Cancelación SAGA")
        void debePreservarAislamientoTenantEnCancelacion() {
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.agregarItem(prod1, 1, Dinero.de(50.0));
            pedido.solicitarReserva();

            // Cancelación por compensación SAGA
            pedido.cancelarPorFaltaDeStock("Rechazo BOD-05 multitenant");

            // Validar que el EmpresaId permanece inalterado e idéntico al original
            assertEquals(empresaId, pedido.getEmpresaId());

            List<DomainEvent> eventos = pedido.drainDomainEvents();
            PedidoCanceladoEvent canceladoEvent = (PedidoCanceladoEvent) eventos.stream()
                    .filter(e -> e instanceof PedidoCanceladoEvent)
                    .findFirst()
                    .orElseThrow();

            assertEquals(empresaId, canceladoEvent.empresaId(), "El evento de integración debe portar el EmpresaId exacto (MT-01)");
        }
    }
}
