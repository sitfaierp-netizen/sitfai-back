package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.event.DiscrepanciaInventarioDetectadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.CantidadFisica;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.ConteoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.EstadoConteo;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.ProductoId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de Pruebas Unitarias Puras para el Agregado {@link ConteoCiclico}.
 * <p>
 * Regla REGLA-1: Cero frameworks externos o contenedores Spring en los tests de Dominio.
 * Ejecución pura sobre JVM con JUnit 5 y AssertJ.
 */
@DisplayName("[Dominio Puro] Agregado ConteoCiclico — Auditoría WMS")
class ConteoCiclicoTest {

    private EmpresaId empresaId;
    private BodegaId bodegaId;
    private ProductoId productoA;
    private ProductoId productoB;
    private LocalDate fechaProgramada;

    @BeforeEach
    void setUp() {
        empresaId = EmpresaId.generar();
        bodegaId = BodegaId.generar();
        productoA = ProductoId.generar();
        productoB = ProductoId.generar();
        fechaProgramada = LocalDate.now().plusDays(1);
    }

    // =========================================================================
    // 1. INVARIANTES DE CREACIÓN Y FACTORY METHODS
    // =========================================================================

    @Nested
    @DisplayName("Invariantes de Creación y Estructura")
    class InvariantesCreacionTest {

        @Test
        @DisplayName("Debe instanciar exitosamente un ConteoCiclico en estado PLANIFICADO")
        void debeInstanciarConteoExitosamente() {
            List<DetalleConteo> detalles = List.of(
                    new DetalleConteo(productoA, 50),
                    new DetalleConteo(productoB, 100)
            );

            ConteoCiclico conteo = ConteoCiclico.iniciar(empresaId, bodegaId, fechaProgramada, detalles);

            assertNotNull(conteo.getId());
            assertEquals(empresaId, conteo.getEmpresaId());
            assertEquals(bodegaId, conteo.getBodegaId());
            assertEquals(fechaProgramada, conteo.getFechaProgramada());
            assertEquals(EstadoConteo.PLANIFICADO, conteo.getEstado());
            assertEquals(2, conteo.getDetalles().size());
            assertThat(conteo.peekDomainEvents()).isEmpty();
            assertNotNull(conteo.getCreadoEn());
            assertNotNull(conteo.getActualizadoEn());
        }

        @Test
        @DisplayName("MT-01: Debe fallar si empresaId es nulo")
        void debeFallarSiEmpresaIdEsNulo() {
            List<DetalleConteo> detalles = List.of(new DetalleConteo(productoA, 10));

            assertThatThrownBy(() -> ConteoCiclico.iniciar(null, bodegaId, fechaProgramada, detalles))
                    .isInstanceOf(NullPointerException.class)
                    .hasMessageContaining("empresaId es obligatorio");
        }

        @Test
        @DisplayName("Debe fallar si no contiene líneas de detalle")
        void debeFallarSiDetallesEstaVacio() {
            assertThatThrownBy(() -> ConteoCiclico.iniciar(empresaId, bodegaId, fechaProgramada, List.of()))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("debe incluir al menos una línea");
        }

        @Test
        @DisplayName("Debe rechazar líneas con productos duplicados en el mismo conteo")
        void debeRechazarProductosDuplicados() {
            List<DetalleConteo> duplicados = List.of(
                    new DetalleConteo(productoA, 20),
                    new DetalleConteo(productoA, 30)
            );

            assertThatThrownBy(() -> ConteoCiclico.iniciar(empresaId, bodegaId, fechaProgramada, duplicados))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("productos duplicados");
        }

        @Test
        @DisplayName("CantidadFisica debe validar fail-fast que el valor no sea negativo")
        void cantidadFisicaDebeRechazarValoresNegativos() {
            assertThatThrownBy(() -> CantidadFisica.de(-1))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("no puede ser negativo");

            assertDoesNotThrow(() -> CantidadFisica.de(0));
            assertDoesNotThrow(() -> CantidadFisica.de(500));
        }

        @Test
        @DisplayName("DetalleConteo debe rechazar cantidadTeorica negativa")
        void detalleConteoDebeRechazarCantidadTeoricaNegativa() {
            assertThatThrownBy(() -> new DetalleConteo(productoA, -5))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("cantidadTeorica no puede ser negativa");
        }
    }

    // =========================================================================
    // 2. REGISTRO DE CONTEO FÍSICO Y TRANSICIÓN A EN_EJECUCION
    // =========================================================================

    @Nested
    @DisplayName("Registro de Conteo Físico")
    class RegistroConteoFisicoTest {

        @Test
        @DisplayName("Debe transicionar a EN_EJECUCION al registrar el primer conteo físico")
        void debeTransicionarAEnEjecucionAlRegistrarConteo() {
            List<DetalleConteo> detalles = List.of(
                    new DetalleConteo(productoA, 50),
                    new DetalleConteo(productoB, 100)
            );
            ConteoCiclico conteo = ConteoCiclico.iniciar(empresaId, bodegaId, fechaProgramada, detalles);

            conteo.registrarConteoFisico(productoA, CantidadFisica.de(50));

            assertEquals(EstadoConteo.EN_EJECUCION, conteo.getEstado());
            DetalleConteo detalleA = conteo.getDetalles().stream()
                    .filter(d -> d.getProductoId().equals(productoA))
                    .findFirst().orElseThrow();
            assertTrue(detalleA.fueContado());
            assertEquals(50, detalleA.getCantidadFisica().valor());
        }

        @Test
        @DisplayName("Debe rechazar registrar conteo para un producto que no está en el ciclo")
        void debeRechazarProductoNoIncluido() {
            List<DetalleConteo> detalles = List.of(new DetalleConteo(productoA, 10));
            ConteoCiclico conteo = ConteoCiclico.iniciar(empresaId, bodegaId, fechaProgramada, detalles);

            ProductoId productoAjeno = ProductoId.generar();

            assertThatThrownBy(() -> conteo.registrarConteoFisico(productoAjeno, CantidadFisica.de(10)))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("no forma parte de este conteo");
        }
    }

    // =========================================================================
    // 3. FINALIZACIÓN SIN DISCREPANCIAS (COMPLETADO)
    // =========================================================================

    @Nested
    @DisplayName("Finalización Exitosa sin Discrepancias (COMPLETADO)")
    class FinalizacionSinDiscrepanciasTest {

        @Test
        @DisplayName("Transiciona a COMPLETADO cuando todas las cantidades físicas coinciden exactamente")
        void debeFinalizarComoCompletadoCuandoCantidadesCuadran() {
            List<DetalleConteo> detalles = List.of(
                    new DetalleConteo(productoA, 40),
                    new DetalleConteo(productoB, 80)
            );
            ConteoCiclico conteo = ConteoCiclico.iniciar(empresaId, bodegaId, fechaProgramada, detalles);

            conteo.registrarConteoFisico(productoA, CantidadFisica.de(40));
            conteo.registrarConteoFisico(productoB, CantidadFisica.de(80));

            conteo.finalizar();

            assertEquals(EstadoConteo.COMPLETADO, conteo.getEstado());
            assertTrue(conteo.getEstado().esFinalizado());
            // No debe haber emitido evento de discrepancia porque no hubo descuadre
            assertThat(conteo.pullDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("Debe impedir registrar conteos físicos posteriores una vez COMPLETADO")
        void debeImpedirModificarConteoFinalizado() {
            List<DetalleConteo> detalles = List.of(new DetalleConteo(productoA, 15));
            ConteoCiclico conteo = ConteoCiclico.iniciar(empresaId, bodegaId, fechaProgramada, detalles);
            conteo.registrarConteoFisico(productoA, CantidadFisica.de(15));
            conteo.finalizar();

            assertThatThrownBy(() -> conteo.registrarConteoFisico(productoA, CantidadFisica.de(20)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no se puede registrar conteo físico en un ciclo finalizado");
        }
    }

    // =========================================================================
    // 4. FINALIZACIÓN CON DISCREPANCIAS (EVENTO DE DOMINIO EMITIDO)
    // =========================================================================

    @Nested
    @DisplayName("Finalización con Discrepancias (CON_DISCREPANCIAS)")
    class FinalizacionConDiscrepanciasTest {

        @Test
        @DisplayName("Transiciona a CON_DISCREPANCIAS y emite DiscrepanciaInventarioDetectadaEvent con detalle")
        void debeDetectarDiscrepanciasYEmitirEvento() {
            List<DetalleConteo> detalles = List.of(
                    new DetalleConteo(productoA, 100), // Teórico: 100, Físico: 95 (Faltante -5)
                    new DetalleConteo(productoB, 50)   // Teórico: 50, Físico: 52 (Sobrante +2)
            );
            ConteoCiclico conteo = ConteoCiclico.iniciar(empresaId, bodegaId, fechaProgramada, detalles);

            conteo.registrarConteoFisico(productoA, CantidadFisica.de(95));
            conteo.registrarConteoFisico(productoB, CantidadFisica.de(52));

            conteo.finalizar();

            assertEquals(EstadoConteo.CON_DISCREPANCIAS, conteo.getEstado());

            List<DiscrepanciaInventarioDetectadaEvent> eventos = conteo.pullDomainEvents();
            assertThat(eventos).hasSize(1);

            DiscrepanciaInventarioDetectadaEvent evento = eventos.get(0);
            assertNotNull(evento.eventoId());
            assertNotNull(evento.ocurridoEn());
            assertEquals(empresaId, evento.empresaId());
            assertEquals(conteo.getId(), evento.conteoId());
            assertEquals(bodegaId, evento.bodegaId());
            assertEquals(2, evento.discrepancias().size());

            // Detalle Producto A (faltante)
            var discA = evento.discrepancias().stream()
                    .filter(d -> d.productoId().equals(productoA))
                    .findFirst().orElseThrow();
            assertEquals(100, discA.cantidadTeorica());
            assertEquals(95, discA.cantidadFisica());
            assertEquals(-5, discA.diferencia());

            // Detalle Producto B (sobrante)
            var discB = evento.discrepancias().stream()
                    .filter(d -> d.productoId().equals(productoB))
                    .findFirst().orElseThrow();
            assertEquals(50, discB.cantidadTeorica());
            assertEquals(52, discB.cantidadFisica());
            assertEquals(2, discB.diferencia());

            // Drenado de eventos: pullDomainEvents debe dejar la lista vacía
            assertThat(conteo.pullDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("Debe fallar al finalizar si alguna línea no ha sido contada")
        void debeFallarSiQuedanLineasSinContar() {
            List<DetalleConteo> detalles = List.of(
                    new DetalleConteo(productoA, 10),
                    new DetalleConteo(productoB, 20)
            );
            ConteoCiclico conteo = ConteoCiclico.iniciar(empresaId, bodegaId, fechaProgramada, detalles);

            // Solo contamos A, pero B queda pendiente
            conteo.registrarConteoFisico(productoA, CantidadFisica.de(10));

            assertThatThrownBy(conteo::finalizar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("no tiene cantidad física registrada");
        }

        @Test
        @DisplayName("Debe rechazar finalizar dos veces el mismo ciclo")
        void debeRechazarDobleFinalizacion() {
            List<DetalleConteo> detalles = List.of(new DetalleConteo(productoA, 10));
            ConteoCiclico conteo = ConteoCiclico.iniciar(empresaId, bodegaId, fechaProgramada, detalles);
            conteo.registrarConteoFisico(productoA, CantidadFisica.de(8));
            conteo.finalizar();

            assertThatThrownBy(conteo::finalizar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("ya se encuentra finalizado");
        }
    }
}
