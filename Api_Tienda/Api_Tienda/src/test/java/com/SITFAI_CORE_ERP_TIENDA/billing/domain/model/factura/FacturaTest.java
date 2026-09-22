package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.FacturaAnuladaEvent;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.FacturaEmitidaEvent;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception.FacturaInvalidaException;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.port.FacturaRepository;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.EstadoFactura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Impuesto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Dominio: Agregado Factura (Billing Puerto 8085)")
class FacturaTest {

    private final EmpresaId empresaId = EmpresaId.generar();
    private final EmpresaId otroEmpresaId = EmpresaId.generar();
    private final ClienteId clienteId = ClienteId.generar();
    private final DocumentoFuenteId docFuente = DocumentoFuenteId.ecommerce(UUID.randomUUID().toString());
    private final FacturaId facturaId = FacturaId.generar();

    private List<LineaFactura> lineasValidas;

    @BeforeEach
    void setUp() {
        lineasValidas = new ArrayList<>();
        // Línea 1: 2 unidades a $100.00 COP = $200.00 subtotal, IVA 19% = $38.00 -> Total línea: $238.00
        LineaFactura linea1 = LineaFactura.crear("Suscripción Mensual", new BigDecimal("2"), Dinero.de(new BigDecimal("100.00")));
        linea1.agregarImpuesto("IVA", new BigDecimal("19"));

        // Línea 2: 1 unidad a $50.00 COP = $50.00 subtotal, sin impuestos -> Total línea: $50.00
        LineaFactura linea2 = LineaFactura.crear("Despacho a Domicilio", new BigDecimal("1"), Dinero.de(new BigDecimal("50.00")));

        lineasValidas.add(linea1);
        lineasValidas.add(linea2);
    }

    @Nested
    @DisplayName("Invariantes de Creación y Emisión (Factory Method)")
    class EmisionTests {

        @Test
        @DisplayName("Debe emitir la factura en estado EMITIDA y calcular totales matemáticos exactos")
        void debeEmitirFacturaYCalcularTotalesExactos() {
            Factura factura = Factura.emitir(facturaId, empresaId, docFuente, clienteId, lineasValidas);

            assertNotNull(factura.getId());
            assertEquals(facturaId, factura.getId());
            assertEquals(empresaId, factura.getEmpresaId());
            assertEquals(docFuente, factura.getDocumentoFuenteId());
            assertEquals(clienteId, factura.getClienteId());
            assertEquals(EstadoFactura.EMITIDA, factura.getEstado());
            assertEquals(2, factura.getLineas().size());

            // Cálculos matemáticos:
            // Subtotal: 2*100 + 1*50 = 250.00
            assertEquals(new BigDecimal("250.00"), factura.getSubtotal().monto());
            // Impuestos: 38.00
            assertEquals(new BigDecimal("38.00"), factura.getTotalImpuestos().monto());
            // Total General: 250.00 + 38.00 = 288.00
            assertEquals(new BigDecimal("288.00"), factura.getTotal().monto());
            assertNotNull(factura.getEmitidoEn());
            assertNull(factura.getAnuladoEn());
            assertNull(factura.getMotivoAnulacion());
        }

        @Test
        @DisplayName("Debe emitir FacturaEmitidaEvent con EmpresaId (MT-01) y datos del documento")
        void debeEmitirEventoFacturaEmitida() {
            Factura factura = Factura.emitir(facturaId, empresaId, docFuente, clienteId, lineasValidas);

            List<DomainEvent> eventos = factura.drainDomainEvents();
            assertThat(eventos).hasSize(1);
            assertThat(eventos.get(0)).isInstanceOf(FacturaEmitidaEvent.class);

            FacturaEmitidaEvent evento = (FacturaEmitidaEvent) eventos.get(0);
            assertNotNull(evento.eventoId());
            assertNotNull(evento.ocurridoEn());
            assertEquals(facturaId.valor(), evento.facturaId());
            assertEquals(empresaId.valor(), evento.empresaId(), "El evento debe portar el EmpresaId (MT-01)");
            assertEquals(docFuente.tipo(), evento.documentoFuenteTipo());
            assertEquals(docFuente.numero(), evento.documentoFuenteNumero());
            assertEquals(new BigDecimal("288.00"), evento.totalFacturado());

            // Verificar que la lista interna fue drenada
            assertTrue(factura.getDomainEvents().isEmpty());
        }

        @Test
        @DisplayName("Fail-Fast: No debe permitir emitir una factura sin líneas de detalle")
        void noDebeEmitirFacturaSinLineas() {
            List<LineaFactura> vacias = Collections.emptyList();
            assertThrows(FacturaInvalidaException.class, () ->
                    Factura.emitir(facturaId, empresaId, docFuente, clienteId, vacias));
        }

        @Test
        @DisplayName("Fail-Fast: No debe permitir emitir con lista de líneas null")
        void noDebeEmitirFacturaConLineasNull() {
            assertThrows(FacturaInvalidaException.class, () ->
                    Factura.emitir(facturaId, empresaId, docFuente, clienteId, null));
        }

        @Test
        @DisplayName("Fail-Fast: No debe permitir emitir sin EmpresaId (Violación MT-01)")
        void noDebeEmitirSinEmpresaId() {
            assertThrows(FacturaInvalidaException.class, () ->
                    Factura.emitir(facturaId, null, docFuente, clienteId, lineasValidas));
        }

        @Test
        @DisplayName("Fail-Fast: No debe permitir emitir sin DocumentoFuenteId (Violación AUD-04)")
        void noDebeEmitirSinDocumentoFuenteId() {
            assertThrows(FacturaInvalidaException.class, () ->
                    Factura.emitir(facturaId, empresaId, null, clienteId, lineasValidas));
        }

        @Test
        @DisplayName("Fail-Fast: No debe permitir emitir sin ClienteId")
        void noDebeEmitirSinClienteId() {
            assertThrows(FacturaInvalidaException.class, () ->
                    Factura.emitir(facturaId, empresaId, docFuente, null, lineasValidas));
        }
    }

    @Nested
    @DisplayName("Inmutabilidad Financiera y Anulación (AUD-04)")
    class AnulacionTests {

        @Test
        @DisplayName("Debe anular factura en estado EMITIDA, registrar motivo, fecha y emitir FacturaAnuladaEvent")
        void debeAnularFacturaEmitida() {
            Factura factura = Factura.emitir(facturaId, empresaId, docFuente, clienteId, lineasValidas);
            factura.drainDomainEvents(); // Limpiar evento de emisión

            String motivo = "Error en digitación de ítems por el cajero";
            factura.anular(motivo);

            assertEquals(EstadoFactura.ANULADA, factura.getEstado());
            assertEquals(motivo, factura.getMotivoAnulacion());
            assertNotNull(factura.getAnuladoEn());

            List<DomainEvent> eventos = factura.drainDomainEvents();
            assertThat(eventos).hasSize(1);
            assertThat(eventos.get(0)).isInstanceOf(FacturaAnuladaEvent.class);

            FacturaAnuladaEvent evento = (FacturaAnuladaEvent) eventos.get(0);
            assertNotNull(evento.eventoId());
            assertNotNull(evento.ocurridoEn());
            assertEquals(facturaId.valor(), evento.facturaId());
            assertEquals(empresaId.valor(), evento.empresaId(), "El evento de anulación debe portar EmpresaId (MT-01)");
            assertEquals(motivo, evento.motivo());
        }

        @Test
        @DisplayName("Fail-Fast: No se puede anular una factura ya anulada (Invariante de Inmutabilidad AUD-04)")
        void noDebeAnularFacturaYaAnulada() {
            Factura factura = Factura.emitir(facturaId, empresaId, docFuente, clienteId, lineasValidas);
            factura.anular("Primera anulación");

            assertEquals(EstadoFactura.ANULADA, factura.getEstado());

            assertThrows(FacturaInvalidaException.class, () ->
                    factura.anular("Segunda anulación duplicada"));
        }

        @Test
        @DisplayName("Fail-Fast: No se puede anular con motivo nulo o vacío")
        void noDebeAnularSinMotivo() {
            Factura factura = Factura.emitir(facturaId, empresaId, docFuente, clienteId, lineasValidas);

            assertThrows(FacturaInvalidaException.class, () -> factura.anular(null));
            assertThrows(FacturaInvalidaException.class, () -> factura.anular("   "));
        }
    }

    @Nested
    @DisplayName("Aislamiento Multi-Tenant (MT-01) en Persistencia (SPI FacturaRepository)")
    class MultiTenantRepositoryTests {

        static class InMemoryFacturaRepository implements FacturaRepository {
            private final Map<String, Factura> store = new HashMap<>();

            private String key(FacturaId id, EmpresaId empresaId) {
                return empresaId.valor() + ":" + id.valor();
            }

            @Override
            public Factura guardar(Factura factura) {
                store.put(key(factura.getId(), factura.getEmpresaId()), factura);
                return factura;
            }

            @Override
            public Optional<Factura> buscarPorId(FacturaId id, EmpresaId empresaId) {
                return Optional.ofNullable(store.get(key(id, empresaId)));
            }

            @Override
            public List<Factura> buscarPorEmpresa(EmpresaId empresaId) {
                return store.values().stream()
                        .filter(f -> f.getEmpresaId().equals(empresaId))
                        .toList();
            }

            @Override
            public boolean existePorId(FacturaId id, EmpresaId empresaId) {
                return store.containsKey(key(id, empresaId));
            }
        }

        @Test
        @DisplayName("MT-01: Repositorio encuentra factura solo cuando coincide el EmpresaId exacto")
        void debeAislarFacturasPorTenant() {
            InMemoryFacturaRepository repo = new InMemoryFacturaRepository();
            Factura facturaTenant1 = Factura.emitir(facturaId, empresaId, docFuente, clienteId, lineasValidas);
            repo.guardar(facturaTenant1);

            // Búsqueda con el tenant legítimo
            Optional<Factura> encontrada = repo.buscarPorId(facturaId, empresaId);
            assertTrue(encontrada.isPresent());
            assertEquals(empresaId, encontrada.get().getEmpresaId());

            // Intento de búsqueda con otro EmpresaId (violación de frontera de tenant)
            Optional<Factura> intentoCrossTenant = repo.buscarPorId(facturaId, otroEmpresaId);
            assertTrue(intentoCrossTenant.isEmpty(), "Un tenant jamás debe ver datos de otra empresa (MT-01)");
        }

        @Test
        @DisplayName("MT-01: Listado por empresa solo retorna facturas del tenant solicitado")
        void debeListarSoloFacturasDelTenant() {
            InMemoryFacturaRepository repo = new InMemoryFacturaRepository();

            Factura f1 = Factura.emitir(FacturaId.generar(), empresaId, docFuente, clienteId, lineasValidas);
            Factura f2 = Factura.emitir(FacturaId.generar(), otroEmpresaId, docFuente, clienteId, lineasValidas);

            repo.guardar(f1);
            repo.guardar(f2);

            List<Factura> facturasTenant1 = repo.buscarPorEmpresa(empresaId);
            assertEquals(1, facturasTenant1.size());
            assertEquals(f1.getId(), facturasTenant1.get(0).getId());

            List<Factura> facturasTenant2 = repo.buscarPorEmpresa(otroEmpresaId);
            assertEquals(1, facturasTenant2.size());
            assertEquals(f2.getId(), facturasTenant2.get(0).getId());
        }
    }
}
