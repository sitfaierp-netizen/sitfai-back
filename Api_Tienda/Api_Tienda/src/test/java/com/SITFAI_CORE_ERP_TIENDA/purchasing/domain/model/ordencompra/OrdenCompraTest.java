package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.event.OrdenCompraEmitidaEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.EstadoOrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.ProveedorId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias puras de Dominio para el Agregado {@link OrdenCompra} (BOD-04, MT-01).
 * <p>
 * Valida la máquina de estados, protección de invariantes, aislamiento multitenant y emisión de eventos
 * en estricto cumplimiento con Clean Architecture (sin dependencias de frameworks).
 */
@DisplayName("Purchasing: OrdenCompra Domain Tests")
class OrdenCompraTest {

    private final EmpresaId tenantId = EmpresaId.de(UUID.randomUUID());
    private final ProveedorId proveedorId = ProveedorId.de(UUID.randomUUID());
    private final OrdenCompraId ordenId = OrdenCompraId.generar();

    @Nested
    @DisplayName("1. Inicialización y Estado Borrador")
    class CreacionTests {

        @Test
        @DisplayName("Debe inicializar la Orden de Compra en estado BORRADOR sin líneas")
        void testCrearOrdenEnBorrador() {
            OrdenCompra orden = OrdenCompra.crear(ordenId, tenantId, proveedorId);

            assertNotNull(orden);
            assertEquals(ordenId, orden.getId());
            assertEquals(tenantId, orden.getEmpresaId());
            assertEquals(proveedorId, orden.getProveedorId());
            assertEquals(EstadoOrdenCompra.BORRADOR, orden.getEstado());
            assertTrue(orden.getLineas().isEmpty());
            assertTrue(orden.getDomainEvents().isEmpty());
            assertNotNull(orden.getCreadoEn());
            assertNull(orden.getEmitidoEn());
            assertEquals(BigDecimal.ZERO, orden.calcularTotalEsperado());
        }

        @Test
        @DisplayName("Debe rechazar identificadores nulos en la inicialización (MT-01 fail-fast)")
        void testFailFastCreacionNula() {
            assertThrows(NullPointerException.class, () -> OrdenCompra.crear(null, tenantId, proveedorId));
            assertThrows(NullPointerException.class, () -> OrdenCompra.crear(ordenId, null, proveedorId));
            assertThrows(NullPointerException.class, () -> OrdenCompra.crear(ordenId, tenantId, null));
        }
    }

    @Nested
    @DisplayName("2. Gestión de Líneas y Cálculos Financieros")
    class LineasTests {

        @Test
        @DisplayName("Debe agregar líneas y calcular subtotales y totales matemáticamente exactos")
        void testAgregarLineasYCalcularTotal() {
            OrdenCompra orden = OrdenCompra.crear(ordenId, tenantId, proveedorId);
            ProductoId prod1 = ProductoId.generar();
            ProductoId prod2 = ProductoId.generar();

            orden.agregarLinea(prod1, 10, new BigDecimal("12.5000"));
            orden.agregarLinea(prod2, 5, new BigDecimal("30.0000"));

            assertEquals(2, orden.getLineas().size());

            LineaOrdenCompra l1 = orden.getLineas().get(0);
            assertEquals(prod1, l1.getProductoId());
            assertEquals(10, l1.getCantidadSolicitada());
            assertEquals(new BigDecimal("125.0000"), l1.getSubtotalEsperado());

            LineaOrdenCompra l2 = orden.getLineas().get(1);
            assertEquals(prod2, l2.getProductoId());
            assertEquals(5, l2.getCantidadSolicitada());
            assertEquals(new BigDecimal("150.0000"), l2.getSubtotalEsperado());

            // 125.0000 + 150.0000 = 275.0000
            assertEquals(new BigDecimal("275.0000"), orden.calcularTotalEsperado());
        }

        @Test
        @DisplayName("Debe rechazar líneas con cantidad menor o igual a cero")
        void testFailFastCantidadInvalida() {
            OrdenCompra orden = OrdenCompra.crear(ordenId, tenantId, proveedorId);
            ProductoId prod = ProductoId.generar();

            assertThrows(IllegalArgumentException.class, () ->
                    orden.agregarLinea(prod, 0, new BigDecimal("10.0000"))
            );
            assertThrows(IllegalArgumentException.class, () ->
                    orden.agregarLinea(prod, -5, new BigDecimal("10.0000"))
            );
        }

        @Test
        @DisplayName("Debe rechazar líneas con costo negativo")
        void testFailFastCostoNegativo() {
            OrdenCompra orden = OrdenCompra.crear(ordenId, tenantId, proveedorId);
            ProductoId prod = ProductoId.generar();

            assertThrows(IllegalArgumentException.class, () ->
                    orden.agregarLinea(prod, 5, new BigDecimal("-1.0000"))
            );
        }
    }

    @Nested
    @DisplayName("3. Emisión al Proveedor y Notificación a Bodega")
    class EmisionTests {

        @Test
        @DisplayName("Debe emitir al proveedor exitosamente, cambiar a EMITIDA y registrar OrdenCompraEmitidaEvent")
        void testEmitirAlProveedorExitoso() {
            OrdenCompra orden = OrdenCompra.crear(ordenId, tenantId, proveedorId);
            orden.agregarLinea(ProductoId.generar(), 20, new BigDecimal("50.0000"));

            orden.emitirAlProveedor();

            assertEquals(EstadoOrdenCompra.EMITIDA, orden.getEstado());
            assertNotNull(orden.getEmitidoEn());

            List<DomainEvent> events = orden.getDomainEvents();
            assertThat(events).hasSize(1);
            assertThat(events.getFirst()).isInstanceOf(OrdenCompraEmitidaEvent.class);

            OrdenCompraEmitidaEvent event = (OrdenCompraEmitidaEvent) events.getFirst();
            assertEquals(tenantId, event.empresaId());
            assertEquals(ordenId, event.ordenCompraId());
            assertEquals(proveedorId, event.proveedorId());
            assertEquals(1, event.totalLineas());
            assertEquals(new BigDecimal("1000.0000"), event.montoTotalEsperado());
        }

        @Test
        @DisplayName("Fail-fast: Prohibido emitir orden de compra vacía sin líneas")
        void testFailFastEmitirSinLineas() {
            OrdenCompra orden = OrdenCompra.crear(ordenId, tenantId, proveedorId);

            IllegalStateException ex = assertThrows(IllegalStateException.class, orden::emitirAlProveedor);
            assertTrue(ex.getMessage().contains("sin líneas de detalle"));
            assertEquals(EstadoOrdenCompra.BORRADOR, orden.getEstado());
        }

        @Test
        @DisplayName("Inmutabilidad: Prohibido agregar líneas a una orden ya EMITIDA")
        void testProhibidoModificarOrdenEmitida() {
            OrdenCompra orden = OrdenCompra.crear(ordenId, tenantId, proveedorId);
            orden.agregarLinea(ProductoId.generar(), 5, new BigDecimal("10.0000"));
            orden.emitirAlProveedor();

            assertThrows(IllegalStateException.class, () ->
                    orden.agregarLinea(ProductoId.generar(), 2, new BigDecimal("15.0000"))
            );
        }

        @Test
        @DisplayName("Invariante: Prohibido re-emitir una orden que ya fue EMITIDA")
        void testProhibidoReemitir() {
            OrdenCompra orden = OrdenCompra.crear(ordenId, tenantId, proveedorId);
            orden.agregarLinea(ProductoId.generar(), 5, new BigDecimal("10.0000"));
            orden.emitirAlProveedor();

            assertThrows(IllegalStateException.class, orden::emitirAlProveedor);
        }
    }

    @Nested
    @DisplayName("4. Ciclo de Vida: Recepciones y Cancelación")
    class CicloDeVidaTests {

        @Test
        @DisplayName("Permite transicionar de EMITIDA a RECEPCION_PARCIAL y luego a COMPLETADA")
        void testTransicionRecepciones() {
            OrdenCompra orden = OrdenCompra.crear(ordenId, tenantId, proveedorId);
            orden.agregarLinea(ProductoId.generar(), 10, new BigDecimal("5.0000"));
            orden.emitirAlProveedor();

            orden.marcarRecepcionParcial();
            assertEquals(EstadoOrdenCompra.RECEPCION_PARCIAL, orden.getEstado());

            orden.marcarCompletada();
            assertEquals(EstadoOrdenCompra.COMPLETADA, orden.getEstado());
        }

        @Test
        @DisplayName("Permite cancelar una orden en BORRADOR o EMITIDA")
        void testCancelarOrden() {
            OrdenCompra orden1 = OrdenCompra.crear(tenantId, proveedorId);
            orden1.cancelar("Presupuesto recortado");
            assertEquals(EstadoOrdenCompra.CANCELADA, orden1.getEstado());

            OrdenCompra orden2 = OrdenCompra.crear(tenantId, proveedorId);
            orden2.agregarLinea(ProductoId.generar(), 1, new BigDecimal("100.0000"));
            orden2.emitirAlProveedor();
            orden2.cancelar("Proveedor sin capacidad");
            assertEquals(EstadoOrdenCompra.CANCELADA, orden2.getEstado());
        }

        @Test
        @DisplayName("Prohibido cancelar una orden que ya fue COMPLETADA")
        void testProhibidoCancelarCompletada() {
            OrdenCompra orden = OrdenCompra.crear(tenantId, proveedorId);
            orden.agregarLinea(ProductoId.generar(), 5, new BigDecimal("10.0000"));
            orden.emitirAlProveedor();
            orden.marcarCompletada();

            assertThrows(IllegalStateException.class, () -> orden.cancelar("Intento tardío"));
        }
    }

    @Nested
    @DisplayName("5. Value Objects de Purchasing")
    class ValueObjectsTests {

        @Test
        @DisplayName("OrdenCompraId, ProveedorId y EmpresaId: inmutabilidad y validaciones")
        void testValueObjects() {
            UUID raw = UUID.randomUUID();

            OrdenCompraId ocId1 = OrdenCompraId.de(raw);
            OrdenCompraId ocId2 = OrdenCompraId.de(raw.toString());
            assertEquals(ocId1, ocId2);

            ProveedorId provId1 = ProveedorId.de(raw);
            ProveedorId provId2 = ProveedorId.de(raw.toString());
            assertEquals(provId1, provId2);

            EmpresaId empId1 = EmpresaId.de(raw);
            EmpresaId empId2 = EmpresaId.de(raw.toString());
            assertEquals(empId1, empId2);

            assertThrows(NullPointerException.class, () -> new OrdenCompraId(null));
            assertThrows(NullPointerException.class, () -> new ProveedorId(null));
            assertThrows(NullPointerException.class, () -> new EmpresaId(null));
        }
    }
}
