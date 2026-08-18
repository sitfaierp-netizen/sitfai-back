package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FacturaElectronicaTest {

    private EmpresaId empresaId;
    private Nit nitEmisor;
    private Nit nitReceptor;
    private ResolucionDian resolucionValida;
    private FacturaId facturaId;

    @BeforeEach
    void setUp() {
        empresaId = new EmpresaId(UUID.randomUUID());
        nitEmisor = new Nit("900123456");
        nitReceptor = new Nit("800987654");
        resolucionValida = new ResolucionDian("SETT", 1, 10000, LocalDate.now().plusMonths(6));
        facturaId = new FacturaId(UUID.randomUUID());
    }

    @Test
    void dadoValoresValidos_cuandoGenerarBorrador_entoncesCreaFacturaEnBorrador() {
        FacturaElectronica factura = FacturaElectronica.generarBorrador(facturaId, empresaId, nitEmisor, nitReceptor, resolucionValida);
        
        assertEquals(EstadoFacturaDian.BORRADOR, factura.getEstado());
        assertEquals(Dinero.cero(), factura.getSubtotal());
        assertEquals(Dinero.cero(), factura.getTotalImpuestos());
        assertEquals(Dinero.cero(), factura.getTotalGeneral());
    }

    @Test
    void dadoFacturaConLineas_cuandoCalcularTotales_entoncesSumaMatematicaExacta() {
        FacturaElectronica factura = FacturaElectronica.generarBorrador(facturaId, empresaId, nitEmisor, nitReceptor, resolucionValida);
        
        LineaFactura linea1 = new LineaFactura("Servicio Nube", new BigDecimal("2"), Dinero.de(50000.00));
        linea1.agregarImpuesto("IVA", new BigDecimal("19")); // 19% de 100,000 = 19,000

        LineaFactura linea2 = new LineaFactura("Soporte Técnico", new BigDecimal("1"), Dinero.de(150000.00));
        linea2.agregarImpuesto("IVA", new BigDecimal("19")); // 19% de 150,000 = 28,500
        linea2.agregarImpuesto("RETEFUENTE", new BigDecimal("11")); // 11% de 150,000 = 16,500

        factura.agregarLinea(linea1);
        factura.agregarLinea(linea2);
        
        // Subtotal = (2 * 50000) + (1 * 150000) = 250000
        assertEquals(new BigDecimal("250000.00"), factura.getSubtotal().monto());
        
        // Impuestos = 19000 + 28500 + 16500 = 64000
        assertEquals(new BigDecimal("64000.00"), factura.getTotalImpuestos().monto());
        
        // Total General = 250000 + 64000 = 314000
        assertEquals(new BigDecimal("314000.00"), factura.getTotalGeneral().monto());
    }

    @Test
    void dadoFacturaLista_cuandoFirmar_entoncesCambiaEstadoYEmiteEvento() {
        FacturaElectronica factura = FacturaElectronica.generarBorrador(facturaId, empresaId, nitEmisor, nitReceptor, resolucionValida);
        factura.agregarLinea(new LineaFactura("Producto", new BigDecimal("1"), Dinero.de(100)));
        
        Cufe cufe = new Cufe("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456"); // 96 chars
        factura.firmar(cufe);
        
        assertEquals(EstadoFacturaDian.FIRMADA, factura.getEstado());
        assertEquals(1, factura.getDomainEvents().size());
        assertEquals(cufe, factura.getCufe());
    }

    @Test
    void dadoFacturaSinLineas_cuandoFirmar_entoncesLanzaExcepcion() {
        FacturaElectronica factura = FacturaElectronica.generarBorrador(facturaId, empresaId, nitEmisor, nitReceptor, resolucionValida);
        Cufe cufe = new Cufe("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456");
        
        assertThrows(DomainException.class, () -> factura.firmar(cufe));
    }
}
