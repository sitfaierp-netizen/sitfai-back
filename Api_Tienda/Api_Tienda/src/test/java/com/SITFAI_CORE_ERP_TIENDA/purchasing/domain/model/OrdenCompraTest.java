package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.enums.DocumentStatus;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.*;
import com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception.DocumentStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrdenCompraTest {

    private OrdenCompraId ordenId;
    private UUID empresaId;
    private ProveedorId proveedorId;
    private String createdBy;

    @BeforeEach
    void setUp() {
        ordenId = OrdenCompraId.generar();
        empresaId = UUID.randomUUID();
        proveedorId = new ProveedorId(UUID.randomUUID());
        createdBy = "user123";
    }

    @Test
    void dadoDatosValidos_cuandoCrear_entoncesOrdenEstadoBorradorYTotalCero() {
        OrdenCompra orden = OrdenCompra.crear(ordenId, empresaId, proveedorId, createdBy);

        assertEquals(DocumentStatus.BORRADOR, orden.getEstado());
        assertEquals(0, BigDecimal.ZERO.compareTo(orden.getTotalMonetario().monto()));
        assertTrue(orden.getLineas().isEmpty());
        assertEquals(1, orden.pullDomainEvents().size());
    }

    @Test
    void dadoOrdenEnBorrador_cuandoAgregarLinea_entoncesSumaAlCostoTotalConCuatroDecimales() {
        OrdenCompra orden = OrdenCompra.crear(ordenId, empresaId, proveedorId, createdBy);
        orden.pullDomainEvents(); // Clear initial event

        ProductoId prod1 = new ProductoId(UUID.randomUUID());
        LineaOrdenCompra linea1 = new LineaOrdenCompra(UUID.randomUUID(), prod1, 2, new Dinero(new BigDecimal("50.1234")));
        
        orden.agregarLinea(linea1);

        assertEquals(1, orden.getLineas().size());
        assertEquals(0, new BigDecimal("100.2468").compareTo(orden.getTotalMonetario().monto())); // 50.1234 * 2
    }

    @Test
    void dadoOrdenSinLineas_cuandoEmitir_entoncesLanzaDocumentStateException() {
        OrdenCompra orden = OrdenCompra.crear(ordenId, empresaId, proveedorId, createdBy);

        assertThrows(DocumentStateException.class, orden::emitir);
    }

    @Test
    void dadoOrdenConLineas_cuandoEmitir_entoncesCambiaEstado() {
        OrdenCompra orden = OrdenCompra.crear(ordenId, empresaId, proveedorId, createdBy);
        orden.agregarLinea(new LineaOrdenCompra(UUID.randomUUID(), new ProductoId(UUID.randomUUID()), 1, new Dinero(BigDecimal.TEN)));
        
        orden.emitir();

        assertEquals(DocumentStatus.EMITIDO, orden.getEstado());
    }

    @Test
    void dadoOrdenEmitida_cuandoAgregarLinea_entoncesLanzaDocumentStateException() {
        OrdenCompra orden = OrdenCompra.crear(ordenId, empresaId, proveedorId, createdBy);
        orden.agregarLinea(new LineaOrdenCompra(UUID.randomUUID(), new ProductoId(UUID.randomUUID()), 1, new Dinero(BigDecimal.TEN)));
        orden.emitir();

        assertThrows(DocumentStateException.class, () -> 
            orden.agregarLinea(new LineaOrdenCompra(UUID.randomUUID(), new ProductoId(UUID.randomUUID()), 1, new Dinero(BigDecimal.TEN)))
        );
    }
}
