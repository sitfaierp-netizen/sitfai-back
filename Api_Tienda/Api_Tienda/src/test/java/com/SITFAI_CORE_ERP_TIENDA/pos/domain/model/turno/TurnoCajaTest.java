package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.TurnoCerradoEvent;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias puras para el Agregado TurnoCaja (Clean Architecture / Dominio puro).
 * <p>
 * Valida reglas de negocio CAJ-02 a CAJ-07, DOC-01, MONEY-01 y MT-01.
 */
class TurnoCajaTest {

    private EmpresaId empresaId;
    private CajaId cajaId;
    private CajeroId cajeroId;
    private Dinero montoApertura;

    @BeforeEach
    void setUp() {
        empresaId = EmpresaId.of(UUID.randomUUID());
        cajaId = CajaId.generar();
        cajeroId = CajeroId.generar();
        montoApertura = Dinero.de("100.0000");
    }

    @Test
    @DisplayName("CAJ-02: Apertura de turno debe inicializar en estado ABIERTO con su monto inicial")
    void abrirTurno_DeberiaInicializarEnEstadoAbierto() {
        TurnoCaja turno = TurnoCaja.abrir(empresaId, cajaId, cajeroId, montoApertura);

        assertNotNull(turno.getId());
        assertEquals(empresaId, turno.getEmpresaId());
        assertEquals(cajaId, turno.getCajaId());
        assertEquals(cajeroId, turno.getCajeroId());
        assertEquals(EstadoTurno.ABIERTO, turno.getEstado());
        assertEquals(montoApertura, turno.getMontoApertura());
        assertNull(turno.getMontoCierre());
        assertNull(turno.getArqueo());
        assertTrue(turno.getTransacciones().isEmpty());
        assertTrue(turno.getDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("CAJ-05: El cálculo teórico debe sumar Ventas e Ingresos, y restar Devoluciones y Egresos")
    void registrarTransacciones_DeberiaCalcularTotalTeoricoCorrectamente() {
        TurnoCaja turno = TurnoCaja.abrir(empresaId, cajaId, cajeroId, montoApertura);

        // Apertura: 100.00
        turno.registrarTransaccion(TipoTransaccionCaja.VENTA, Dinero.de("150.5000"), "TICKET-001");
        turno.registrarTransaccion(TipoTransaccionCaja.INGRESO, Dinero.de("50.0000"), "ING-001");
        turno.registrarTransaccion(TipoTransaccionCaja.DEVOLUCION, Dinero.de("20.2500"), "DEV-001");
        turno.registrarTransaccion(TipoTransaccionCaja.EGRESO, Dinero.de("30.0000"), "EGR-001");

        // 100 + 150.50 + 50 - 20.25 - 30 = 250.25
        Dinero totalEsperado = Dinero.de("250.2500");
        assertEquals(totalEsperado, turno.calcularTotalTeorico());
        assertEquals(4, turno.getTransacciones().size());
    }

    @Test
    @DisplayName("CAJ-06 & CAJ-07: Cierre de turno exacto (sin descuadre) emite TurnoCerradoEvent y arqueo cuadrado")
    void cerrarTurno_ConMontoDeclaradoExacto_DeberiaCerrarSinDescuadre() {
        TurnoCaja turno = TurnoCaja.abrir(empresaId, cajaId, cajeroId, montoApertura);
        turno.registrarTransaccion(TipoTransaccionCaja.VENTA, Dinero.de("200.0000"), "TICKET-002");

        // Total teórico: 100 + 200 = 300
        Dinero montoDeclarado = Dinero.de("300.0000");
        ArqueoCaja arqueo = turno.cerrar(montoDeclarado);

        assertEquals(EstadoTurno.CERRADO, turno.getEstado());
        assertEquals(montoDeclarado, turno.getMontoCierre());
        assertNotNull(arqueo);
        assertEquals(Dinero.de("300.0000"), arqueo.totalTeoricoEsperado());
        assertEquals(montoDeclarado, arqueo.montoFisicoDeclarado());
        assertTrue(arqueo.esCuadrado());
        assertEquals(Dinero.cero(), arqueo.descuadre());

        // Validar emisión inmutable del Domain Event
        assertEquals(1, turno.getDomainEvents().size());
        TurnoCerradoEvent evento = (TurnoCerradoEvent) turno.getDomainEvents().get(0);
        assertEquals(turno.getId(), evento.turnoId());
        assertEquals(empresaId, evento.empresaId());
        assertEquals(cajaId, evento.cajaId());
        assertEquals(cajeroId, evento.cajeroId());
        assertEquals(montoApertura, evento.montoApertura());
        assertEquals(Dinero.de("200.0000"), evento.totalVentas());
        assertEquals(Dinero.de("300.0000"), evento.totalTeoricoEsperado());
        assertEquals(montoDeclarado, evento.montoFisicoDeclarado());
        assertEquals(Dinero.cero(), evento.descuadre());
    }

    @Test
    @DisplayName("CAJ-07: Cierre con sobrante (monto declarado > total teórico)")
    void cerrarTurno_ConSobrante_DeberiaRegistrarDescuadrePositivo() {
        TurnoCaja turno = TurnoCaja.abrir(empresaId, cajaId, cajeroId, montoApertura);
        turno.registrarTransaccion(TipoTransaccionCaja.VENTA, Dinero.de("100.0000"), "TICKET-003");

        // Teórico: 200.00, Declarado: 210.00 -> Sobrante: +10.00
        Dinero declarado = Dinero.de("210.0000");
        ArqueoCaja arqueo = turno.cerrar(declarado);

        assertTrue(arqueo.tieneSobrante());
        assertFalse(arqueo.tieneFaltante());
        assertFalse(arqueo.esCuadrado());
        assertEquals(Dinero.de("10.0000"), arqueo.descuadre());
    }

    @Test
    @DisplayName("CAJ-07: Cierre con faltante (monto declarado < total teórico)")
    void cerrarTurno_ConFaltante_DeberiaRegistrarDescuadreNegativo() {
        TurnoCaja turno = TurnoCaja.abrir(empresaId, cajaId, cajeroId, montoApertura);
        turno.registrarTransaccion(TipoTransaccionCaja.VENTA, Dinero.de("100.0000"), "TICKET-004");

        // Teórico: 200.00, Declarado: 185.00 -> Faltante: -15.00
        Dinero declarado = Dinero.de("185.0000");
        ArqueoCaja arqueo = turno.cerrar(declarado);

        assertTrue(arqueo.tieneFaltante());
        assertFalse(arqueo.tieneSobrante());
        assertFalse(arqueo.esCuadrado());
        assertEquals(Dinero.de("-15.0000"), arqueo.descuadre());
    }

    @Test
    @DisplayName("CAJ-03: Invariante Fail-Fast: Registrar transacciones en turno CERRADO debe fallar")
    void registrarTransaccion_EnTurnoCerrado_DeberiaLanzarExcepcion() {
        TurnoCaja turno = TurnoCaja.abrir(empresaId, cajaId, cajeroId, montoApertura);
        turno.cerrar(Dinero.de("100.0000"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                turno.registrarTransaccion(TipoTransaccionCaja.VENTA, Dinero.de("50.0000"), "TICKET-FAIL")
        );

        assertTrue(ex.getMessage().contains("Fail-fast"));
        assertTrue(ex.getMessage().contains("CERRADO"));
    }

    @Test
    @DisplayName("DOC-01: Intentar cerrar un turno ya cerrado debe lanzar excepción")
    void cerrarTurno_YaCerrado_DeberiaLanzarExcepcion() {
        TurnoCaja turno = TurnoCaja.abrir(empresaId, cajaId, cajeroId, montoApertura);
        turno.cerrar(Dinero.de("100.0000"));

        assertThrows(IllegalStateException.class, () ->
                turno.cerrar(Dinero.de("100.0000"))
        );
    }

    @Test
    @DisplayName("DOC-01: La lista de transacciones del agregado debe ser inmutable hacia el exterior")
    void getTransacciones_DeberiaSerInmutable() {
        TurnoCaja turno = TurnoCaja.abrir(empresaId, cajaId, cajeroId, montoApertura);
        turno.registrarTransaccion(TipoTransaccionCaja.VENTA, Dinero.de("10.0000"), "TICKET-005");

        assertThrows(UnsupportedOperationException.class, () ->
                turno.getTransacciones().add(TransaccionCaja.registrar(TipoTransaccionCaja.VENTA, Dinero.de("5.0000"), "HACK"))
        );
    }

    @Test
    @DisplayName("CAJ-02: Apertura con monto negativo debe ser rechazada")
    void abrirTurno_ConMontoNegativo_DeberiaLanzarExcepcion() {
        Dinero montoNegativo = Dinero.de("-50.0000");
        assertThrows(IllegalArgumentException.class, () ->
                TurnoCaja.abrir(empresaId, cajaId, cajeroId, montoNegativo)
        );
    }

    @Test
    @DisplayName("MONEY-01: Operaciones de Dinero deben respetar redondeo HALF_UP a 4 decimales")
    void dinero_OperacionesMatematicas_DeberianRespetarHalfUp() {
        Dinero d1 = Dinero.de(new BigDecimal("10.12345")); // redondea HALF_UP a 10.1235
        assertEquals(new BigDecimal("10.1235"), d1.monto());

        Dinero d2 = Dinero.de("5.0000");
        Dinero multiplicacion = d2.multiplicar(new BigDecimal("1.33335")); // 5 * 1.33335 = 6.66675 -> 6.6668
        assertEquals(new BigDecimal("6.6668"), multiplicacion.monto());

        Dinero division = Dinero.de("10.0000").dividir(new BigDecimal("3")); // 3.3333
        assertEquals(new BigDecimal("3.3333"), division.monto());
    }
}
