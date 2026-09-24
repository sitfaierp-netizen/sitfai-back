package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.event.ProductoReclasificadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.CategoriaABC;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.MetricaMovimiento;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.ProductoId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Test Unitario Puro: ClasificacionProducto (Aggregate Root).
 * <p>
 * Regla REGLA-8: Tests de dominio con JUnit 5 puro — SIN Spring context,
 * SIN Mockito, SIN Testcontainers. Solo lógica de dominio.
 * Cobertura objetivo: ≥ 90% del Domain Layer.
 */
@DisplayName("ClasificacionProducto — Aggregate Root")
class ClasificacionProductoTest {

    private EmpresaId  empresaId;
    private BodegaId   bodegaId;
    private ProductoId productoId;

    @BeforeEach
    void setUp() {
        empresaId  = new EmpresaId(UUID.randomUUID());
        bodegaId   = new BodegaId(UUID.randomUUID());
        productoId = new ProductoId(UUID.randomUUID());
    }

    // =========================================================================
    // FACTORY METHOD — iniciar()
    // =========================================================================
    @Nested
    @DisplayName("Factory Method: iniciar()")
    class FactoryMethodIniciar {

        @Test
        @DisplayName("Debe crear en estado NO_CLASIFICADO con métrica sin movimientos")
        void debeCrearEnEstadoNoClasificado() {
            ClasificacionProducto cp = ClasificacionProducto.iniciar(empresaId, bodegaId, productoId);

            assertThat(cp.getId()).isNotNull();
            assertThat(cp.getEmpresaId()).isEqualTo(empresaId);
            assertThat(cp.getBodegaId()).isEqualTo(bodegaId);
            assertThat(cp.getProductoId()).isEqualTo(productoId);
            assertThat(cp.getCategoria()).isEqualTo(CategoriaABC.NO_CLASIFICADO);
            assertThat(cp.getMetrica().frecuenciaSalida()).isZero();
            assertThat(cp.getMetrica().valorTotalDespachado()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(cp.estaClasificado()).isFalse();
            assertThat(cp.getCreadoEn()).isNotNull();
        }

        @Test
        @DisplayName("No debe emitir eventos al ser creado (no hubo cambio de categoría)")
        void noDebeEmitirEventosAlCrearse() {
            ClasificacionProducto cp = ClasificacionProducto.iniciar(empresaId, bodegaId, productoId);
            assertThat(cp.peekDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("Debe rechazar empresaId null — MT-01")
        void debeRechazarEmpresaIdNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClasificacionProducto.iniciar(null, bodegaId, productoId))
                    .withMessageContaining("MT-01");
        }

        @Test
        @DisplayName("Debe rechazar bodegaId null")
        void debeRechazarBodegaIdNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClasificacionProducto.iniciar(empresaId, null, productoId));
        }

        @Test
        @DisplayName("Debe rechazar productoId null")
        void debeRechazarProductoIdNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ClasificacionProducto.iniciar(empresaId, bodegaId, null));
        }
    }

    // =========================================================================
    // MÉTODO DE NEGOCIO — reclasificar()
    // =========================================================================
    @Nested
    @DisplayName("Comportamiento: reclasificar()")
    class Reclasificar {

        @Test
        @DisplayName("Primera clasificación de NO_CLASIFICADO a A debe emitir evento")
        void primeraClasificacionDebeEmitirEvento() {
            ClasificacionProducto cp = ClasificacionProducto.iniciar(empresaId, bodegaId, productoId);
            MetricaMovimiento metrica = MetricaMovimiento.de(150, new BigDecimal("45000.00"));

            cp.reclasificar(metrica, CategoriaABC.A);

            assertThat(cp.getCategoria()).isEqualTo(CategoriaABC.A);
            assertThat(cp.estaClasificado()).isTrue();
            List<ProductoReclasificadoEvent> eventos = cp.peekDomainEvents();
            assertThat(eventos).hasSize(1);
            assertThat(eventos.get(0).categoriaAnterior()).isEqualTo(CategoriaABC.NO_CLASIFICADO);
            assertThat(eventos.get(0).categoriaNueva()).isEqualTo(CategoriaABC.A);
        }

        @Test
        @DisplayName("Cambio de B a A debe emitir evento con categorías correctas")
        void cambioDeBaADebeEmitirEvento() {
            ClasificacionProducto cp = ClasificacionProducto.iniciar(empresaId, bodegaId, productoId);
            cp.reclasificar(MetricaMovimiento.de(50, new BigDecimal("5000")), CategoriaABC.B);
            cp.pullDomainEvents(); // consumir evento anterior

            cp.reclasificar(MetricaMovimiento.de(200, new BigDecimal("80000")), CategoriaABC.A);

            List<ProductoReclasificadoEvent> eventos = cp.peekDomainEvents();
            assertThat(eventos).hasSize(1);
            ProductoReclasificadoEvent evento = eventos.get(0);
            assertThat(evento.categoriaAnterior()).isEqualTo(CategoriaABC.B);
            assertThat(evento.categoriaNueva()).isEqualTo(CategoriaABC.A);
            assertThat(evento.empresaId()).isEqualTo(empresaId);
            assertThat(evento.bodegaId()).isEqualTo(bodegaId);
            assertThat(evento.productoId()).isEqualTo(productoId);
            assertThat(evento.eventoId()).isNotNull();
            assertThat(evento.ocurridoEn()).isNotNull();
        }

        @Test
        @DisplayName("Reclasificación con la MISMA categoría NO debe emitir evento")
        void mismaCategoriaNoDebeEmitirEvento() {
            ClasificacionProducto cp = ClasificacionProducto.iniciar(empresaId, bodegaId, productoId);
            MetricaMovimiento m1 = MetricaMovimiento.de(100, new BigDecimal("10000"));
            cp.reclasificar(m1, CategoriaABC.B);
            cp.pullDomainEvents(); // consumir primer evento

            // Misma categoría B, métricas diferentes
            MetricaMovimiento m2 = MetricaMovimiento.de(110, new BigDecimal("11000"));
            cp.reclasificar(m2, CategoriaABC.B);

            assertThat(cp.getCategoria()).isEqualTo(CategoriaABC.B);
            assertThat(cp.getMetrica().frecuenciaSalida()).isEqualTo(110); // métrica actualizada
            assertThat(cp.peekDomainEvents()).isEmpty(); // sin evento
        }

        @Test
        @DisplayName("La métrica se actualiza siempre, independientemente del cambio de categoría")
        void metricaSiempreSeActualiza() {
            ClasificacionProducto cp = ClasificacionProducto.iniciar(empresaId, bodegaId, productoId);
            MetricaMovimiento nueva = MetricaMovimiento.de(75, new BigDecimal("7500.5000"));

            cp.reclasificar(nueva, CategoriaABC.C);

            assertThat(cp.getMetrica().frecuenciaSalida()).isEqualTo(75);
            assertThat(cp.getMetrica().valorTotalDespachado())
                    .isEqualByComparingTo(new BigDecimal("7500.5000"));
        }

        @Test
        @DisplayName("Múltiples reclasificaciones generan eventos acumulados hasta pullDomainEvents()")
        void multipleReclasificacionesAcumulanEventos() {
            ClasificacionProducto cp = ClasificacionProducto.iniciar(empresaId, bodegaId, productoId);

            cp.reclasificar(MetricaMovimiento.de(10, BigDecimal.TEN), CategoriaABC.C);
            cp.reclasificar(MetricaMovimiento.de(50, new BigDecimal("500")), CategoriaABC.B);
            cp.reclasificar(MetricaMovimiento.de(200, new BigDecimal("20000")), CategoriaABC.A);

            assertThat(cp.peekDomainEvents()).hasSize(3);

            List<ProductoReclasificadoEvent> consumidos = cp.pullDomainEvents();
            assertThat(consumidos).hasSize(3);
            assertThat(cp.peekDomainEvents()).isEmpty(); // limpieza efectiva
        }

        @Test
        @DisplayName("Debe rechazar nuevaMetrica null")
        void debeRechazarMetricaNull() {
            ClasificacionProducto cp = ClasificacionProducto.iniciar(empresaId, bodegaId, productoId);
            assertThatNullPointerException()
                    .isThrownBy(() -> cp.reclasificar(null, CategoriaABC.A));
        }

        @Test
        @DisplayName("Debe rechazar nuevaCategoria null")
        void debeRechazarCategoriaNull() {
            ClasificacionProducto cp = ClasificacionProducto.iniciar(empresaId, bodegaId, productoId);
            assertThatNullPointerException()
                    .isThrownBy(() -> cp.reclasificar(MetricaMovimiento.sinMovimientos(), null));
        }
    }

    // =========================================================================
    // VALUE OBJECT: MetricaMovimiento
    // =========================================================================
    @Nested
    @DisplayName("Value Object: MetricaMovimiento — Inmutabilidad y Validaciones")
    class MetricaMovimientoTest {

        @Test
        @DisplayName("Debe rechazar frecuencia negativa (fail-fast)")
        void debeRechazarFrecuenciaNegativa() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> MetricaMovimiento.de(-1, BigDecimal.TEN))
                    .withMessageContaining("frecuenciaSalida");
        }

        @Test
        @DisplayName("Debe rechazar valor total despachado negativo (fail-fast)")
        void debeRechazarValorNegativo() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> MetricaMovimiento.de(10, new BigDecimal("-0.01")))
                    .withMessageContaining("valorTotalDespachado");
        }

        @Test
        @DisplayName("Debe rechazar valor total null — MONEY-01")
        void debeRechazarValorNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> MetricaMovimiento.de(10, null))
                    .withMessageContaining("MONEY-01");
        }

        @Test
        @DisplayName("Debe normalizar la escala a DECIMAL(19,4) con RoundingMode.HALF_UP")
        void debeNormalizarEscalaMonetaria() {
            MetricaMovimiento m = MetricaMovimiento.de(1, new BigDecimal("100.123456789"));
            assertThat(m.valorTotalDespachado().scale()).isEqualTo(4);
            assertThat(m.valorTotalDespachado())
                    .isEqualByComparingTo(new BigDecimal("100.1235")); // HALF_UP
        }

        @Test
        @DisplayName("Frecuencia cero con valor cero es válido (sin movimientos)")
        void frecuenciaCeroEsValida() {
            MetricaMovimiento m = MetricaMovimiento.sinMovimientos();
            assertThat(m.frecuenciaSalida()).isZero();
            assertThat(m.valorTotalDespachado()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(m.tieneMovimientos()).isFalse();
        }

        @Test
        @DisplayName("tieneMovimientos debe retornar true si hay frecuencia > 0")
        void tieneMovimientosCuandoFrecuenciaPositiva() {
            assertThat(MetricaMovimiento.de(1, BigDecimal.ZERO).tieneMovimientos()).isTrue();
        }

        @Test
        @DisplayName("tieneMovimientos debe retornar true si hay valor > 0")
        void tieneMovimientosCuandoValorPositivo() {
            assertThat(MetricaMovimiento.de(0, BigDecimal.ONE).tieneMovimientos()).isTrue();
        }

        @Test
        @DisplayName("Los records son inmutables — igualdad por valor")
        void recordsIgualdadPorValor() {
            MetricaMovimiento m1 = MetricaMovimiento.de(100, new BigDecimal("5000.0000"));
            MetricaMovimiento m2 = MetricaMovimiento.de(100, new BigDecimal("5000.0000"));
            assertThat(m1).isEqualTo(m2);
            assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        }
    }

    // =========================================================================
    // VALUE OBJECT: CategoriaABC
    // =========================================================================
    @Nested
    @DisplayName("Enum: CategoriaABC")
    class CategoriaABCTest {

        @Test
        @DisplayName("estaClasificado debe retornar false solo para NO_CLASIFICADO")
        void estaClasificadoSoloFalseParaNoClasificado() {
            assertThat(CategoriaABC.NO_CLASIFICADO.estaClasificado()).isFalse();
            assertThat(CategoriaABC.A.estaClasificado()).isTrue();
            assertThat(CategoriaABC.B.estaClasificado()).isTrue();
            assertThat(CategoriaABC.C.estaClasificado()).isTrue();
        }
    }

    // =========================================================================
    // EVENTO DE DOMINIO: ProductoReclasificadoEvent
    // =========================================================================
    @Nested
    @DisplayName("Domain Event: ProductoReclasificadoEvent — Validaciones de Invariante")
    class ProductoReclasificadoEventTest {

        @Test
        @DisplayName("Debe rechazar evento con categoriaAnterior igual a categoriaNueva")
        void debeRechazarCategoriasIguales() {
            ClasificacionProducto cp = ClasificacionProducto.iniciar(empresaId, bodegaId, productoId);

            assertThatIllegalArgumentException()
                    .isThrownBy(() -> ProductoReclasificadoEvent.of(
                            empresaId,
                            bodegaId,
                            productoId,
                            cp.getId(),
                            CategoriaABC.A,
                            CategoriaABC.A  // igual — inválido
                    ))
                    .withMessageContaining("no pueden ser iguales");
        }

        @Test
        @DisplayName("Debe auto-asignar eventoId y ocurridoEn si se pasan null")
        void debeAutoAsignarCamposDeSistema() {
            ClasificacionProducto cp = ClasificacionProducto.iniciar(empresaId, bodegaId, productoId);

            ProductoReclasificadoEvent evento = new ProductoReclasificadoEvent(
                    null, null,
                    empresaId, bodegaId, productoId, cp.getId(),
                    CategoriaABC.NO_CLASIFICADO, CategoriaABC.B
            );

            assertThat(evento.eventoId()).isNotNull();
            assertThat(evento.ocurridoEn()).isNotNull();
        }

        @Test
        @DisplayName("El evento captura correctamente todos los datos del cambio")
        void debeCapturarDatosDelCambio() {
            ClasificacionProducto cp = ClasificacionProducto.iniciar(empresaId, bodegaId, productoId);
            cp.reclasificar(MetricaMovimiento.de(5, new BigDecimal("500")), CategoriaABC.C);

            ProductoReclasificadoEvent evento = cp.peekDomainEvents().get(0);

            assertThat(evento.empresaId()).isEqualTo(empresaId);
            assertThat(evento.bodegaId()).isEqualTo(bodegaId);
            assertThat(evento.productoId()).isEqualTo(productoId);
            assertThat(evento.clasificacionId()).isEqualTo(cp.getId());
            assertThat(evento.categoriaAnterior()).isEqualTo(CategoriaABC.NO_CLASIFICADO);
            assertThat(evento.categoriaNueva()).isEqualTo(CategoriaABC.C);
        }
    }

    // =========================================================================
    // RECONSTITUCIÓN (reconstituir())
    // =========================================================================
    @Nested
    @DisplayName("Reconstitución desde persistencia")
    class Reconstitucion {

        @Test
        @DisplayName("La reconstitución no debe generar eventos de dominio")
        void reconstitucionNoDebeGenerarEventos() {
            ClasificacionProducto original = ClasificacionProducto.iniciar(empresaId, bodegaId, productoId);
            original.reclasificar(MetricaMovimiento.de(20, BigDecimal.TEN), CategoriaABC.B);

            ClasificacionProducto reconstituido = ClasificacionProducto.reconstituir(
                    original.getId(),
                    original.getEmpresaId(),
                    original.getBodegaId(),
                    original.getProductoId(),
                    original.getMetrica(),
                    original.getCategoria(),
                    original.getCreadoEn()
            );

            assertThat(reconstituido.getCategoria()).isEqualTo(CategoriaABC.B);
            assertThat(reconstituido.peekDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("Debe preservar todos los campos originales")
        void debePreservarCamposOriginales() {
            ClasificacionProducto cp = ClasificacionProducto.iniciar(empresaId, bodegaId, productoId);
            MetricaMovimiento metrica = MetricaMovimiento.de(99, new BigDecimal("9999.9999"));
            cp.reclasificar(metrica, CategoriaABC.A);

            ClasificacionProducto r = ClasificacionProducto.reconstituir(
                    cp.getId(), empresaId, bodegaId, productoId, metrica, CategoriaABC.A, cp.getCreadoEn());

            assertThat(r.getId()).isEqualTo(cp.getId());
            assertThat(r.getEmpresaId()).isEqualTo(empresaId);
            assertThat(r.getCategoria()).isEqualTo(CategoriaABC.A);
            assertThat(r.getMetrica().frecuenciaSalida()).isEqualTo(99);
        }
    }
}
