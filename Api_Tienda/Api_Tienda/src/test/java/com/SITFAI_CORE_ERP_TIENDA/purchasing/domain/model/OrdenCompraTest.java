package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrdenCompraTest {

    private OrdenCompraId ordenId;
    private EmpresaId empresaId;
    private ProveedorId proveedorId;

    @BeforeEach
    void setUp() {
        ordenId = new OrdenCompraId(UUID.randomUUID());
        empresaId = new EmpresaId(UUID.randomUUID());
        proveedorId = new ProveedorId(UUID.randomUUID());
    }

    @Test
    void dadoDatosValidos_cuandoCrearBorrador_entoncesOrdenEstadoBorradorYTotalCero() {
        OrdenCompra orden = OrdenCompra.crearBorrador(ordenId, empresaId, proveedorId);

        assertEquals(EstadoOrden.BORRADOR, orden.getEstado());
        assertEquals(new BigDecimal("0.0000"), orden.getCostoTotal().monto());
        assertTrue(orden.getLineas().isEmpty());
    }

    @Test
    void dadoOrdenEnBorrador_cuandoAgregarLinea_entoncesSumaAlCostoTotalConCuatroDecimales() {
        OrdenCompra orden = OrdenCompra.crearBorrador(ordenId, empresaId, proveedorId);

        ProductoId prod1 = new ProductoId(UUID.randomUUID());
        LineaOrdenCompra linea1 = new LineaOrdenCompra(prod1, new BigDecimal("2"), Dinero.de(50.1234));
        
        orden.agregarLinea(linea1);

        assertEquals(1, orden.getLineas().size());
        assertEquals(new BigDecimal("100.2468"), orden.getCostoTotal().monto()); // 50.1234 * 2
    }

    @Test
    void dadoOrdenSinLineas_cuandoEmitir_entoncesLanzaDomainException() {
        OrdenCompra orden = OrdenCompra.crearBorrador(ordenId, empresaId, proveedorId);

        assertThrows(DomainException.class, orden::emitir);
    }

    @Test
    void dadoOrdenConLineas_cuandoEmitir_entoncesCambiaEstado() {
        OrdenCompra orden = OrdenCompra.crearBorrador(ordenId, empresaId, proveedorId);
        orden.agregarLinea(new LineaOrdenCompra(new ProductoId(UUID.randomUUID()), BigDecimal.ONE, Dinero.de(10)));
        
        orden.emitir();

        assertEquals(EstadoOrden.EMITIDA, orden.getEstado());
    }

    @Test
    void dadoOrdenEmitida_cuandoAgregarLinea_entoncesLanzaDomainException() {
        OrdenCompra orden = OrdenCompra.crearBorrador(ordenId, empresaId, proveedorId);
        orden.agregarLinea(new LineaOrdenCompra(new ProductoId(UUID.randomUUID()), BigDecimal.ONE, Dinero.de(10)));
        orden.emitir();

        assertThrows(DomainException.class, () -> 
            orden.agregarLinea(new LineaOrdenCompra(new ProductoId(UUID.randomUUID()), BigDecimal.ONE, Dinero.de(10)))
        );
    }

    @Test
    void dadoOrdenEmitida_cuandoRecibir_entoncesCambiaEstadoYGeneraEvento() {
        OrdenCompra orden = OrdenCompra.crearBorrador(ordenId, empresaId, proveedorId);
        orden.agregarLinea(new LineaOrdenCompra(new ProductoId(UUID.randomUUID()), BigDecimal.ONE, Dinero.de(10)));
        orden.emitir();
        
        orden.marcarComoRecibida();

        assertEquals(EstadoOrden.RECIBIDA, orden.getEstado());
        assertEquals(1, orden.getDomainEvents().size());
    }
}
