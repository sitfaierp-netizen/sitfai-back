package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.event.PedidoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EstadoPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ProductoId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pruebas Unitarias Puras para el Agregado {@link Pedido}.
 * <p>
 * Regla REGLA-8: Pruebas de Dominio puras sin contexto de Spring ni frameworks.
 * Valida:
 * - Creación e invariantes en estado PENDIENTE.
 * - Cálculo monetario de subtotales y total (MONEY-01, HALF_UP).
 * - Transición de estado a CONFIRMADO y registro de {@link PedidoConfirmadoEvent}.
 * - Drenado de eventos de dominio (pullDomainEvents).
 * - Guardias e invariantes de negocio (rechazo de pedidos vacíos o cancelados).
 */
@DisplayName("Dominio Puro: Agregado Pedido (Api_Tienda / pedido)")
class PedidoTest {

    private EmpresaId empresaId;
    private ClienteId clienteId;
    private ProductoId producto1;
    private ProductoId producto2;

    @BeforeEach
    void setUp() {
        empresaId = EmpresaId.generar();
        clienteId = ClienteId.generar();
        producto1 = ProductoId.generar();
        producto2 = ProductoId.generar();
    }

    @Nested
    @DisplayName("Creación e Invariantes Iniciales")
    class CreacionTests {

        @Test
        @DisplayName("Debe instanciar el pedido en estado PENDIENTE con lista de líneas vacía y total cero")
        void debeInstanciarPedidoEnEstadoPendiente() {
            Pedido pedido = Pedido.crear(empresaId, clienteId);

            assertThat(pedido.getId()).isNotNull();
            assertThat(pedido.getEmpresaId()).isEqualTo(empresaId);
            assertThat(pedido.getClienteId()).isEqualTo(clienteId);
            assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
            assertThat(pedido.getLineas()).isEmpty();
            assertThat(pedido.calcularTotal()).isEqualTo(Dinero.cero());
            assertThat(pedido.peekDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("Debe rechazar la creación si empresaId o clienteId son nulos (MT-01)")
        void debeRechazarParametrosNulosEnCreacion() {
            assertThatThrownBy(() -> Pedido.crear(null, clienteId))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("empresaId");

            assertThatThrownBy(() -> Pedido.crear(empresaId, null))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("clienteId");
        }
    }

    @Nested
    @DisplayName("Gestión de Líneas y Cálculo de Total")
    class LineasYCalculoTests {

        @Test
        @DisplayName("Debe agregar líneas y calcular el total correctamente (MONEY-01)")
        void debeAgregarLineasYCalcularTotal() {
            Pedido pedido = Pedido.crear(empresaId, clienteId);

            // Línea 1: 2 unidades x 10.50 = 21.00
            pedido.agregarLinea(producto1, 2, Dinero.de(10.50));
            // Línea 2: 3 unidades x 20.00 = 60.00
            pedido.agregarLinea(producto2, 3, Dinero.de(20.00));

            assertThat(pedido.getLineas()).hasSize(2);
            assertThat(pedido.calcularTotal().monto())
                    .isEqualByComparingTo(new BigDecimal("81.00"));
        }

        @Test
        @DisplayName("Debe rechazar línea con cantidad menor o igual a cero")
        void debeRechazarCantidadInvalida() {
            Pedido pedido = Pedido.crear(empresaId, clienteId);

            assertThatThrownBy(() -> pedido.agregarLinea(producto1, 0, Dinero.de(10.00)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("estrictamente positiva");

            assertThatThrownBy(() -> pedido.agregarLinea(producto1, -1, Dinero.de(10.00)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Debe rechazar Dinero con montos negativos")
        void debeRechazarDineroNegativo() {
            assertThatThrownBy(() -> Dinero.de(-5.00))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negativo");
        }

        @Test
        @DisplayName("Debe aplicar RoundingMode.HALF_UP en Dinero")
        void debeAplicarRedondeoHalfUp() {
            Dinero dinero = Dinero.de(new BigDecimal("10.555"));
            assertThat(dinero.monto()).isEqualTo(new BigDecimal("10.56").setScale(2, RoundingMode.HALF_UP));
        }

        @Test
        @DisplayName("No debe permitir agregar líneas si el pedido ya no está en estado PENDIENTE")
        void noDebePermitirAgregarLineasSiNoEstaPendiente() {
            Pedido pedido = Pedido.crear(empresaId, clienteId);
            pedido.agregarLinea(producto1, 1, Dinero.de(15.00));
            pedido.confirmar();

            assertThatThrownBy(() -> pedido.agregarLinea(producto2, 1, Dinero.de(10.00)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no se puede agregar líneas");
        }
    }

    @Nested
    @DisplayName("Transición de Estado: Confirmar y Eventos")
    class ConfirmacionTests {

        @Test
        @DisplayName("Debe confirmar el pedido, transicionar a CONFIRMADO y registrar PedidoConfirmadoEvent")
        void debeConfirmarPedidoYGenerarEvento() {
            Pedido pedido = Pedido.crear(empresaId, clienteId);
            pedido.agregarLinea(producto1, 2, Dinero.de(50.00));

            pedido.confirmar();

            assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CONFIRMADO);

            List<PedidoConfirmadoEvent> eventos = pedido.pullDomainEvents();
            assertThat(eventos).hasSize(1);

            PedidoConfirmadoEvent evento = eventos.get(0);
            assertThat(evento.empresaId()).isEqualTo(empresaId);
            assertThat(evento.pedidoId()).isEqualTo(pedido.getId());
            assertThat(evento.clienteId()).isEqualTo(clienteId);
            assertThat(evento.total().monto()).isEqualByComparingTo(new BigDecimal("100.00"));
            assertThat(evento.lineas()).hasSize(1);
            assertThat(evento.lineas().get(0).getProductoId()).isEqualTo(producto1);
            assertThat(evento.lineas().get(0).getCantidad()).isEqualTo(2);

            // Al drenar eventos, la lista interna queda limpia
            assertThat(pedido.pullDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("Debe rechazar la confirmación de un pedido sin líneas")
        void debeRechazarConfirmacionSinLineas() {
            Pedido pedido = Pedido.crear(empresaId, clienteId);

            assertThatThrownBy(pedido::confirmar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("sin líneas");
        }

        @Test
        @DisplayName("Debe rechazar la confirmación de un pedido cancelado")
        void debeRechazarConfirmacionDePedidoCancelado() {
            Pedido pedido = Pedido.crear(empresaId, clienteId);
            pedido.agregarLinea(producto1, 1, Dinero.de(10.00));
            pedido.cancelar();

            assertThatThrownBy(pedido::confirmar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("CANCELADO");
        }

        @Test
        @DisplayName("La confirmación de un pedido ya confirmado debe ser idempotente")
        void confirmacionDebeSerIdempotente() {
            Pedido pedido = Pedido.crear(empresaId, clienteId);
            pedido.agregarLinea(producto1, 1, Dinero.de(10.00));
            pedido.confirmar();

            // Consumir el primer evento
            List<PedidoConfirmadoEvent> eventosIniciales = pedido.pullDomainEvents();
            assertThat(eventosIniciales).hasSize(1);

            // Segunda confirmación
            pedido.confirmar();
            assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.CONFIRMADO);
            assertThat(pedido.pullDomainEvents()).isEmpty();
        }
    }
}
