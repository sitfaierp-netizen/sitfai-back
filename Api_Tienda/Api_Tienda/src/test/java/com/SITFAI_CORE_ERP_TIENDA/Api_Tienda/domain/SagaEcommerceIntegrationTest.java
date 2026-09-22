package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.CancelarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.dto.ConfirmarPedidoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.PedidoEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service.CancelarPedidoService;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.service.ConfirmarPedidoService;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoCanceladoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoNoEncontradoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.EstadoPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.port.output.PedidoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.messaging.ReservaRechazadaEventHandler;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.in.messaging.StockReservadoEventHandler;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.ReservaRechazadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.StockReservadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de Integración de la SAGA E-Commerce (Happy Path & Unhappy Path).
 * <p>
 * Valida el ciclo completo de la coreografía entre Inventario y E-Commerce bajo aislamiento estricto MT-01:
 * <ul>
 *   <li><b>Happy Path:</b> Reserva exitosa en Inventario → Emisión de {@link StockReservadoEvent}
 *       → Recepción en {@link StockReservadoEventHandler} → {@link ConfirmarPedidoService}
 *       → Transición a {@link EstadoPedido#CONFIRMADO} → Emisión de {@link PedidoConfirmadoEvent}.</li>
 *   <li><b>Unhappy Path:</b> Falta de stock (BOD-05) → Emisión de {@link ReservaRechazadaEvent}
 *       con {@code EmpresaId} → Recepción en {@link ReservaRechazadaEventHandler} → {@link CancelarPedidoService}
 *       → Transición a {@link EstadoPedido#CANCELADO} → Emisión de {@link PedidoCanceladoEvent}.</li>
 *   <li><b>MT-01:</b> Demostración de frontera estricta entre tenants (las operaciones dirigidas
 *       con otro {@code EmpresaId} son rechazadas inmediatamente por el repositorio).</li>
 * </ul>
 */
@DisplayName("[SAGA] Suite E-Commerce Completa: Happy Path (Confirmación) & Unhappy Path (Compensación) (MT-01)")
class SagaEcommerceIntegrationTest {

    private final EmpresaId empresaId = EmpresaId.generar();
    private final EmpresaId otroEmpresaId = EmpresaId.generar();
    private final ClienteId clienteId = ClienteId.generar();
    private final ProductoId productoId = ProductoId.generar();

    // VOs del BC de Inventario (FQN para evitar colisión de paquetes)
    private final com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId invProductoId =
            new com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId(productoId.valor());

    private final com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId invEmpresaId =
            new com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId(empresaId.valor());

    private final BodegaId bodegaId = BodegaId.generar();

    // ─────────────────────────────────────────────────────────────────────────
    // Repositorio y Publisher In-Memory para simular persistencia y eventos
    // ─────────────────────────────────────────────────────────────────────────

    static class InMemoryPedidoRepository implements PedidoRepository {
        private final Map<String, Pedido> store = new HashMap<>();

        private String key(PedidoId id, EmpresaId empresaId) {
            return empresaId.valor() + ":" + id.valor();
        }

        @Override
        public Pedido guardar(Pedido pedido) {
            store.put(key(pedido.getId(), pedido.getEmpresaId()), pedido);
            return pedido;
        }

        @Override
        public Optional<Pedido> buscarPorId(PedidoId id, EmpresaId empresaId) {
            return Optional.ofNullable(store.get(key(id, empresaId)));
        }

        @Override
        public List<Pedido> buscarPorEmpresa(EmpresaId empresaId) {
            return store.values().stream()
                    .filter(p -> p.getEmpresaId().equals(empresaId))
                    .toList();
        }

        @Override
        public boolean existePorId(PedidoId id, EmpresaId empresaId) {
            return store.containsKey(key(id, empresaId));
        }
    }

    static class InMemoryEventPublisher implements PedidoEventPublisher {
        final List<DomainEvent> published = new ArrayList<>();

        @Override
        public void publicar(DomainEvent event) {
            published.add(event);
        }

        @Override
        public void publicarTodos(List<DomainEvent> events) {
            published.addAll(events);
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // BLOQUE 1: SAGA HAPPY PATH (RESERVA EXITOSA → PEDIDO CONFIRMADO)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("SAGA Happy Path (Confirmación de Pedido tras Reserva de Stock)")
    class HappyPathSagaTest {

        @Test
        @DisplayName("MT-01: StockReservadoEvent debe contener EmpresaId inmutable y DocumentoFuenteId con PedidoId")
        void debeCrearStockReservadoEventConCamposCorrectos() {
            UUID pedidoIdRaw = UUID.randomUUID();
            DocumentoFuenteId docFuente = new DocumentoFuenteId("PEDIDO_ECOMMERCE", pedidoIdRaw.toString());
            com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad cantidad =
                    com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad.de(new BigDecimal("3.00"));

            StockReservadoEvent evento = StockReservadoEvent.of(bodegaId, invProductoId, invEmpresaId, cantidad, docFuente);

            assertNotNull(evento.eventoId());
            assertNotNull(evento.ocurridoEn());
            assertEquals(invEmpresaId, evento.empresaId(), "Debe portar el EmpresaId de inventario (MT-01)");
            assertEquals(empresaId.valor(), evento.empresaId().valor());
            assertEquals("PEDIDO_ECOMMERCE", evento.documentoFuente().tipo());
            assertEquals(pedidoIdRaw.toString(), evento.documentoFuente().numero());
        }

        @Test
        @DisplayName("MT-01: ConfirmarPedidoService confirma y persiste pedido en estado CONFIRMADO bajo tenant correcto")
        void debeConfirmarPedidoExitosamenteCuandoTenantCoincide() {
            InMemoryPedidoRepository repo = new InMemoryPedidoRepository();
            InMemoryEventPublisher publisher = new InMemoryEventPublisher();
            ConfirmarPedidoService service = new ConfirmarPedidoService(repo, publisher);

            // GIVEN: Pedido en RESERVANDO_STOCK
            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.agregarItem(productoId, 2, Dinero.de(75.0));
            pedido.solicitarReserva();
            pedido.drainDomainEvents(); // Aislar evento previo
            repo.guardar(pedido);

            // WHEN: ConfirmarPedidoCommand con el tenant correcto
            ConfirmarPedidoCommand command = new ConfirmarPedidoCommand(
                    empresaId.valor(),
                    pedido.getId().valor()
            );
            service.ejecutar(command);

            // THEN: El pedido en BD/Repo queda en CONFIRMADO
            Optional<Pedido> pedidoGuardado = repo.buscarPorId(pedido.getId(), empresaId);
            assertTrue(pedidoGuardado.isPresent());
            assertEquals(EstadoPedido.CONFIRMADO, pedidoGuardado.get().getEstado());

            // THEN: Se emite PedidoConfirmadoEvent con el EmpresaId correcto
            assertThat(publisher.published).hasSize(1);
            assertThat(publisher.published.get(0)).isInstanceOf(PedidoConfirmadoEvent.class);
            PedidoConfirmadoEvent evento = (PedidoConfirmadoEvent) publisher.published.get(0);
            assertEquals(empresaId, evento.empresaId());
            assertEquals(pedido.getId(), evento.pedidoId());
            assertEquals(new BigDecimal("150.00"), evento.total().monto());
        }

        @Test
        @DisplayName("MT-01: ConfirmarPedidoService rechaza confirmación si se intenta desde otro EmpresaId (Aislamiento Total)")
        void debeRechazarConfirmacionSiTenantNoCoincide() {
            InMemoryPedidoRepository repo = new InMemoryPedidoRepository();
            InMemoryEventPublisher publisher = new InMemoryEventPublisher();
            ConfirmarPedidoService service = new ConfirmarPedidoService(repo, publisher);

            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.agregarItem(productoId, 1, Dinero.de(100.0));
            pedido.solicitarReserva();
            repo.guardar(pedido);

            // Intento con otroEmpresaId
            ConfirmarPedidoCommand commandConOtroTenant = new ConfirmarPedidoCommand(
                    otroEmpresaId.valor(),
                    pedido.getId().valor()
            );

            assertThrows(PedidoNoEncontradoException.class, () -> service.ejecutar(commandConOtroTenant));

            // El pedido original no fue modificado
            Optional<Pedido> pedidoOriginal = repo.buscarPorId(pedido.getId(), empresaId);
            assertTrue(pedidoOriginal.isPresent());
            assertEquals(EstadoPedido.RESERVANDO_STOCK, pedidoOriginal.get().getEstado());
        }

        @Test
        @DisplayName("MT-01: StockReservadoEventHandler procesa evento de inventario y confirma pedido en repositorio")
        void debeProcesarEventoStockReservadoYConfirmarPedido() {
            InMemoryPedidoRepository repo = new InMemoryPedidoRepository();
            InMemoryEventPublisher publisher = new InMemoryEventPublisher();
            ConfirmarPedidoService service = new ConfirmarPedidoService(repo, publisher);
            StockReservadoEventHandler handler = new StockReservadoEventHandler(service);

            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.agregarItem(productoId, 3, Dinero.de(50.0));
            pedido.solicitarReserva();
            repo.guardar(pedido);

            // Evento emitido por Inventario con docFuente apuntando al PedidoId y EmpresaId de tenant
            DocumentoFuenteId docFuente = new DocumentoFuenteId("PEDIDO_ECOMMERCE", pedido.getId().valor().toString());
            com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad cantidad =
                    com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad.de(new BigDecimal("3.00"));

            StockReservadoEvent evento = StockReservadoEvent.of(bodegaId, invProductoId, invEmpresaId, cantidad, docFuente);

            // Ejecución del listener asíncrono
            handler.onStockReservado(evento);

            // El pedido debe haber quedado CONFIRMADO en el repositorio del tenant
            Optional<Pedido> pedidoConfirmado = repo.buscarPorId(pedido.getId(), empresaId);
            assertTrue(pedidoConfirmado.isPresent());
            assertEquals(EstadoPedido.CONFIRMADO, pedidoConfirmado.get().getEstado());
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // BLOQUE 2: SAGA UNHAPPY PATH (RECHAZO DE RESERVA → COMPENSACIÓN)
    // ═════════════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("SAGA Unhappy Path (Compensación de Pedido por Falta de Stock)")
    class UnhappyPathSagaTest {

        @Test
        @DisplayName("MT-01: ReservaRechazadaEvent debe contener EmpresaId y motivo BOD-05")
        void debeCrearReservaRechazadaEventConEmpresaId() {
            UUID pedidoIdRaw = UUID.randomUUID();
            String motivo = "Rechazado por regla BOD-05: Stock insuficiente";

            ReservaRechazadaEvent evento = ReservaRechazadaEvent.of(invEmpresaId, pedidoIdRaw, invProductoId, motivo);

            assertNotNull(evento.eventoId());
            assertNotNull(evento.ocurridoEn());
            assertEquals(invEmpresaId, evento.empresaId(), "El evento debe portar el EmpresaId (MT-01)");
            assertEquals(empresaId.valor(), evento.empresaId().valor());
            assertEquals(pedidoIdRaw, evento.pedidoId());
            assertEquals(invProductoId, evento.productoId());
            assertThat(evento.motivo()).contains("BOD-05");
        }

        @Test
        @DisplayName("MT-01: CancelarPedidoService compensa exitosamente cuando el EmpresaId coincide con el registro")
        void debeCancelarPedidoCuandoTenantCoincide() {
            InMemoryPedidoRepository repo = new InMemoryPedidoRepository();
            InMemoryEventPublisher publisher = new InMemoryEventPublisher();
            CancelarPedidoService service = new CancelarPedidoService(repo, publisher);

            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.agregarItem(productoId, 2, Dinero.de(50.0));
            pedido.solicitarReserva();
            pedido.drainDomainEvents();
            repo.guardar(pedido);

            CancelarPedidoCommand command = new CancelarPedidoCommand(
                    empresaId.valor(),
                    pedido.getId().valor(),
                    "SAGA-COMPENSACION: Stock insuficiente"
            );

            service.ejecutar(command);

            Optional<Pedido> pedidoActualizado = repo.buscarPorId(pedido.getId(), empresaId);
            assertTrue(pedidoActualizado.isPresent());
            assertEquals(EstadoPedido.CANCELADO, pedidoActualizado.get().getEstado());
            assertThat(publisher.published).hasSize(1);
            assertThat(publisher.published.get(0)).isInstanceOf(PedidoCanceladoEvent.class);
        }

        @Test
        @DisplayName("MT-01: Búsqueda acotada por tenant rechaza compensación si se envía otro EmpresaId")
        void debeRechazarCompensacionSiEmpresaIdNoCoincide() {
            InMemoryPedidoRepository repo = new InMemoryPedidoRepository();
            InMemoryEventPublisher publisher = new InMemoryEventPublisher();
            CancelarPedidoService service = new CancelarPedidoService(repo, publisher);

            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.agregarItem(productoId, 2, Dinero.de(50.0));
            pedido.solicitarReserva();
            repo.guardar(pedido);

            CancelarPedidoCommand commandConOtroTenant = new CancelarPedidoCommand(
                    otroEmpresaId.valor(),
                    pedido.getId().valor(),
                    "SAGA-COMPENSACION: Stock insuficiente"
            );

            assertThrows(PedidoNoEncontradoException.class, () -> service.ejecutar(commandConOtroTenant));

            Optional<Pedido> pedidoOriginal = repo.buscarPorId(pedido.getId(), empresaId);
            assertTrue(pedidoOriginal.isPresent());
            assertEquals(EstadoPedido.RESERVANDO_STOCK, pedidoOriginal.get().getEstado());
        }

        @Test
        @DisplayName("MT-01: ReservaRechazadaEventHandler extrae empresaId del evento e invoca cancelación")
        void debePropagarEmpresaIdDesdeHandlerHaciaUseCase() {
            InMemoryPedidoRepository repo = new InMemoryPedidoRepository();
            InMemoryEventPublisher publisher = new InMemoryEventPublisher();
            CancelarPedidoService service = new CancelarPedidoService(repo, publisher);
            ReservaRechazadaEventHandler handler = new ReservaRechazadaEventHandler(service);

            Pedido pedido = Pedido.iniciar(empresaId, clienteId);
            pedido.agregarItem(productoId, 3, Dinero.de(40.0));
            pedido.solicitarReserva();
            repo.guardar(pedido);

            ReservaRechazadaEvent evento = ReservaRechazadaEvent.of(
                    invEmpresaId,
                    pedido.getId().valor(),
                    invProductoId,
                    "Stock insuficiente BOD-05"
            );

            handler.onReservaRechazada(evento);

            Optional<Pedido> pedidoCancelado = repo.buscarPorId(pedido.getId(), empresaId);
            assertTrue(pedidoCancelado.isPresent());
            assertEquals(EstadoPedido.CANCELADO, pedidoCancelado.get().getEstado());
        }
    }
}
