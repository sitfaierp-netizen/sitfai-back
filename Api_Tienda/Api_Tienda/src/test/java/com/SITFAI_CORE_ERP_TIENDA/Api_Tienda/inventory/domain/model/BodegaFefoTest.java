package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.MovimientoRegistradoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.PuntoReordenAlcanzadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.StockActualizadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.StockInsuficienteException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class BodegaFefoTest {

    private EmpresaId empresaId;
    private SucursalId sucursalId;
    private Bodega bodega;
    private ProductoId productoId;
    private DocumentoFuenteId documentoIngreso;
    private DocumentoFuenteId documentoSalida;

    @BeforeEach
    void setUp() {
        empresaId = new EmpresaId(UUID.randomUUID());
        sucursalId = new SucursalId(UUID.randomUUID());
        productoId = new ProductoId(UUID.randomUUID());
        documentoIngreso = new DocumentoFuenteId("COMPRA", "OC-001");
        documentoSalida = new DocumentoFuenteId("VENTA", "VEN-001");

        bodega = Bodega.crear(empresaId, sucursalId, "BOD-01", "Bodega Principal", TipoBodega.VENTA);
        bodega.drainDomainEvents(); // Limpiar eventos de creación
    }

    private Instant toInstant(int year, int month, int day) {
        return ZonedDateTime.of(year, month, day, 0, 0, 0, 0, ZoneId.of("UTC")).toInstant();
    }

    // TEST 01 — FEFO COMPLETO
    @Test
    void testFefoCompleto() {
        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(5)), LoteId.de("LOTE-A"), toInstant(2026, 9, 1), documentoIngreso);
        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(3)), LoteId.de("LOTE-B"), toInstant(2026, 8, 25), documentoIngreso);
        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(10)), LoteId.de("LOTE-C"), toInstant(2026, 10, 1), documentoIngreso);

        assertEquals(BigDecimal.valueOf(18), bodega.consultarStock(productoId));

        bodega.descontarStock(productoId, Cantidad.de(BigDecimal.valueOf(7)), documentoSalida);

        assertEquals(BigDecimal.valueOf(11), bodega.consultarStock(productoId));

        StockLote loteA = bodega.getLotes().stream().filter(l -> l.getLoteId().valor().equals("LOTE-A")).findFirst().get();
        StockLote loteB = bodega.getLotes().stream().filter(l -> l.getLoteId().valor().equals("LOTE-B")).findFirst().get();
        StockLote loteC = bodega.getLotes().stream().filter(l -> l.getLoteId().valor().equals("LOTE-C")).findFirst().get();

        assertEquals(BigDecimal.ZERO, loteB.getCantidad());
        assertEquals(BigDecimal.ONE, loteA.getCantidad());
        assertEquals(BigDecimal.valueOf(10), loteC.getCantidad());
    }

    // TEST 02 — FECHA NULL
    @Test
    void testLotesSinFechaDeCaducidad() {
        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(5)), LoteId.de("LOTE-NULL"), null, documentoIngreso);
        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(5)), LoteId.de("LOTE-FECHA"), toInstant(2026, 9, 1), documentoIngreso);

        bodega.descontarStock(productoId, Cantidad.de(BigDecimal.valueOf(6)), documentoSalida);

        StockLote loteNull = bodega.getLotes().stream().filter(l -> l.getLoteId().valor().equals("LOTE-NULL")).findFirst().get();
        StockLote loteFecha = bodega.getLotes().stream().filter(l -> l.getLoteId().valor().equals("LOTE-FECHA")).findFirst().get();

        assertEquals(BigDecimal.ZERO, loteFecha.getCantidad());
        assertEquals(BigDecimal.valueOf(4), loteNull.getCantidad());
    }

    // TEST 03 — VARIOS LOTES MISMA FECHA
    @Test
    void testLotesMismaFechaDeCaducidad() {
        Instant mismaFecha = toInstant(2026, 9, 1);
        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(5)), LoteId.de("LOTE-1"), mismaFecha, documentoIngreso);
        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(5)), LoteId.de("LOTE-2"), mismaFecha, documentoIngreso);

        bodega.descontarStock(productoId, Cantidad.de(BigDecimal.valueOf(6)), documentoSalida);

        assertEquals(BigDecimal.valueOf(4), bodega.consultarStock(productoId));
    }

    // TEST 04 — STOCK INSUFICIENTE
    @Test
    void testStockInsuficienteException() {
        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(5)), LoteId.de("LOTE-A"), toInstant(2026, 9, 1), documentoIngreso);

        assertThrows(StockInsuficienteException.class, () -> {
            bodega.descontarStock(productoId, Cantidad.de(BigDecimal.valueOf(10)), documentoSalida);
        });
        
        // Garantizar atomicidad
        assertEquals(BigDecimal.valueOf(5), bodega.consultarStock(productoId));
    }

    // TEST 05 — BODEGA INACTIVA
    @Test
    void testBodegaInactivaRechazaMovimientos() {
        bodega.desactivar();
        assertThrows(IllegalStateException.class, () -> {
            bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.ONE), LoteId.de("LOTE-A"), toInstant(2026, 9, 1), documentoIngreso);
        });
        assertThrows(IllegalStateException.class, () -> {
            bodega.descontarStock(productoId, Cantidad.de(BigDecimal.ONE), documentoSalida);
        });
    }

    // TEST 06 y 07 — CANTIDAD CERO O NEGATIVA
    @Test
    void testCantidadesNegativasOCeroLanzanError() {
        assertThrows(IllegalArgumentException.class, () -> {
            Cantidad.de(BigDecimal.ZERO);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            Cantidad.de(BigDecimal.valueOf(-5));
        });
    }

    // TEST 08 — EXACTAMENTE TODO EL STOCK
    @Test
    void testExactamenteTodoElStock() {
        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(5)), LoteId.de("LOTE-A"), toInstant(2026, 9, 1), documentoIngreso);
        bodega.descontarStock(productoId, Cantidad.de(BigDecimal.valueOf(5)), documentoSalida);
        assertEquals(BigDecimal.ZERO, bodega.consultarStock(productoId));
    }

    // TEST 09 — MÚLTIPLES LOTES
    @Test
    void testMultiplesLotesEnCadena() {
        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(2)), LoteId.de("L1"), toInstant(2026, 1, 1), documentoIngreso);
        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(2)), LoteId.de("L2"), toInstant(2026, 2, 1), documentoIngreso);
        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(2)), LoteId.de("L3"), toInstant(2026, 3, 1), documentoIngreso);
        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(2)), LoteId.de("L4"), toInstant(2026, 4, 1), documentoIngreso);

        bodega.descontarStock(productoId, Cantidad.de(BigDecimal.valueOf(7)), documentoSalida);

        assertEquals(BigDecimal.ZERO, bodega.getLotes().stream().filter(l -> l.getLoteId().valor().equals("L1")).findFirst().get().getCantidad());
        assertEquals(BigDecimal.ZERO, bodega.getLotes().stream().filter(l -> l.getLoteId().valor().equals("L2")).findFirst().get().getCantidad());
        assertEquals(BigDecimal.ZERO, bodega.getLotes().stream().filter(l -> l.getLoteId().valor().equals("L3")).findFirst().get().getCantidad());
        assertEquals(BigDecimal.ONE, bodega.getLotes().stream().filter(l -> l.getLoteId().valor().equals("L4")).findFirst().get().getCantidad());
        assertEquals(BigDecimal.ONE, bodega.consultarStock(productoId));
    }

    // TEST 10 — BOD-08 (PUNTO DE REORDEN)
    @Test
    void testPuntoDeReorden() {
        Map<ProductoId, PuntoReorden> puntosReorden = new HashMap<>();
        puntosReorden.put(productoId, PuntoReorden.de(BigDecimal.valueOf(10)));
        Bodega bodegaReorden = Bodega.crear(empresaId, sucursalId, "BOD-02", "Bodega Secundaria", TipoBodega.VENTA);
        
        // Inyectar el punto de reorden vía reconstitución
        bodegaReorden = Bodega.reconstituir(
                bodegaReorden.getId(), 
                empresaId, 
                sucursalId, 
                bodegaReorden.getCodigo(), 
                bodegaReorden.getNombre(), 
                true, 
                TipoBodega.VENTA, 
                java.util.Collections.emptyList(),
                puntosReorden, 
                bodegaReorden.getMovimientos(), 
                bodegaReorden.getCreadoEn(), 
                bodegaReorden.getActualizadoEn(),
                0L,
                true,
                null,
                null
        );

        bodegaReorden.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(15)), LoteId.de("L1"), null, documentoIngreso);
        bodega = Bodega.reconstituir(
                BodegaId.de(UUID.randomUUID()),
                empresaId, 
                sucursalId, 
                "BOD-02", 
                "Bodega Secundaria", 
                true, 
                TipoBodega.VENTA,
                java.util.Collections.emptyList(),
                puntosReorden, 
                java.util.Collections.emptyList(), 
                java.time.Instant.now(), 
                java.time.Instant.now(), 
                0L,
                true,
                null,
                null
        );

        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(15)), LoteId.de("L1"), null, documentoIngreso);
        bodega.drainDomainEvents();

        // Baja a 8 (cruza el umbral de 10)
        bodega.descontarStock(productoId, Cantidad.de(BigDecimal.valueOf(7)), documentoSalida);

        long eventosReorden = bodega.getDomainEvents().stream()
                .filter(e -> e instanceof PuntoReordenAlcanzadoEvent)
                .count();

        assertEquals(1, eventosReorden);
    }

    // TEST 11 — PRODUCTO AISLADO
    @Test
    void testProductoAislado() {
        ProductoId otroProductoId = new ProductoId(UUID.randomUUID());
        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(5)), LoteId.de("L1"), null, documentoIngreso);
        bodega.registrarIngreso(otroProductoId, Cantidad.de(BigDecimal.valueOf(10)), LoteId.de("L2"), null, documentoIngreso);

        bodega.descontarStock(productoId, Cantidad.de(BigDecimal.valueOf(2)), documentoSalida);

        assertEquals(BigDecimal.valueOf(3), bodega.consultarStock(productoId));
        assertEquals(BigDecimal.valueOf(10), bodega.consultarStock(otroProductoId));
    }

    // TEST 12 — EVENTOS
    @Test
    void testEventosTipados() {
        bodega.drainDomainEvents();
        bodega.registrarIngreso(productoId, Cantidad.de(BigDecimal.valueOf(5)), LoteId.de("L1"), null, documentoIngreso);

        assertTrue(bodega.getDomainEvents().stream().anyMatch(e -> e instanceof MovimientoRegistradoEvent));
        assertTrue(bodega.getDomainEvents().stream().anyMatch(e -> e instanceof StockActualizadoEvent));
    }
}
