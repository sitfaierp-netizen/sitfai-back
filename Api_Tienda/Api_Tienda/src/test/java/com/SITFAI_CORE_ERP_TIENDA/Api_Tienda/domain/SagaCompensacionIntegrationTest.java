package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoCanceladoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.EstadoPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.ReservaRechazadaEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de integración (dominio puro - sin Spring) que simula el ciclo completo de la SAGA de Compensación.
 * <p>
 * Verifica el "Unhappy Path": reserva de stock rechazada por BOD-05 → emisión de ReservaRechazadaEvent
 * → cancelación del Pedido → emisión de PedidoCanceladoEvent.
 * <p>
 * Se ejecuta SIN contexto de Spring (Regla 8 — Unit Tests del Dominio son JUnit 5 puro).
 */
@DisplayName("[SAGA] Flujo de Compensación: Reserva Rechazada → Pedido Cancelado")
class SagaCompensacionIntegrationTest {

    private final EmpresaId empresaId = EmpresaId.generar();
    private final ClienteId clienteId = ClienteId.generar();
    private final ProductoId productoId = ProductoId.generar();

    // VO de ProductoId del BC de Inventario — clase diferente al de Api_Tienda (mismo nombre, distinto package).
    // Se usa FQN para evitar colisión de nombres en Java.
    private final com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId invProductoId =
            new com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId(productoId.valor());

    @Nested
    @DisplayName("Emisor de Rechazo (Bounded Context: Inventario)")
    class EmisionReservaRechazadaTest {

        @Test
        @DisplayName("Debe construir ReservaRechazadaEvent con motivo BOD-05 correctamente")
        void debeCrearReservaRechazadaEventConCamposCorretos() {
            UUID pedidoIdRaw = UUID.randomUUID();
            String motivo = "Rechazado por regla BOD-05: Stock insuficiente (disponible: 0, solicitado: 5)";

            ReservaRechazadaEvent evento = ReservaRechazadaEvent.of(pedidoIdRaw, invProductoId, motivo);

            assertNotNull(evento.eventoId(), "El eventoId no debe ser nulo (idempotencia)");
            assertNotNull(evento.ocurridoEn(), "El timestamp no debe ser nulo (AUD-01)");
            assertEquals(pedidoIdRaw, evento.pedidoId());
            assertEquals(invProductoId, evento.productoId());
            assertThat(evento.motivo()).contains("BOD-05");
        }
    }

    @Nested
    @DisplayName("Receptor y Compensador (Bounded Context: E-Commerce/Api_Tienda)")
    class CompensacionPedidoTest {

        @Test
        @DisplayName("[Unhappy Path] Debe cancelar Pedido que estaba RESERVANDO_STOCK y emitir PedidoCanceladoEvent")
        void debeCancelarPedidoEnReservandoStockYEmitirEvento() {
            // GIVEN: Un pedido que ya inició la reserva de stock (estado = RESERVANDO_STOCK)
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.agregarItem(productoId, 5, Dinero.de(150.0));
            pedido.solicitarReserva(); // Estado: RESERVANDO_STOCK, emite PedidoCreadoEvent

            assertEquals(EstadoPedido.RESERVANDO_STOCK, pedido.getEstado());

            // Drena el evento de creación para aislar el siguiente paso
            pedido.drainDomainEvents();

            // WHEN: El handler de compensación recibe el evento de inventario y ejecuta la cancelación
            String motivoSaga = "SAGA-COMPENSACION: Stock insuficiente en Bodega Principal";
            pedido.cancelarPorFaltaDeStock(motivoSaga);

            // THEN: El Pedido debe haber transicionado a CANCELADO
            assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());

            // THEN: El Pedido debe haber emitido el PedidoCanceladoEvent
            List<DomainEvent> eventos = pedido.drainDomainEvents();
            assertThat(eventos).hasSize(1);
            assertThat(eventos.get(0)).isInstanceOf(PedidoCanceladoEvent.class);

            PedidoCanceladoEvent eventoCancelado = (PedidoCanceladoEvent) eventos.get(0);
            assertEquals(pedido.getId(), eventoCancelado.pedidoId());
            assertEquals(empresaId, eventoCancelado.empresaId());
            assertEquals(clienteId, eventoCancelado.clienteId());
            assertThat(eventoCancelado.motivo()).contains("Stock insuficiente");
            assertNotNull(eventoCancelado.eventoId());
            assertNotNull(eventoCancelado.ocurridoEn());
        }

        @Test
        @DisplayName("[Guard] Debe lanzar excepción si se intenta cancelarPorFaltaDeStock en estado diferente a RESERVANDO_STOCK")
        void debeLanzarExcepcionSiEstadoNoEsReservandoStock() {
            // Un pedido en estado inicial CREADO, no en RESERVANDO_STOCK
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.agregarItem(productoId, 5, Dinero.de(150.0));

            PedidoInvalidoException ex = assertThrows(PedidoInvalidoException.class, () ->
                    pedido.cancelarPorFaltaDeStock("Intento inválido de compensación"));

            assertThat(ex.getMessage()).contains("RESERVANDO_STOCK");
            // Estado NO debe haber mutado
            assertEquals(EstadoPedido.CREADO, pedido.getEstado());
        }

        @Test
        @DisplayName("[Guard] Un pedido ya CANCELADO no puede ser cancelado nuevamente")
        void noDebeCancelarPedidoYaCancelado() {
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.cancelar("Primera cancelación manual");

            assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());

            // Intento de doble cancelación — debe fallar fail-fast
            assertThrows(PedidoInvalidoException.class, () ->
                    pedido.cancelar("Segunda cancelación duplicada"));
        }
    }
}
