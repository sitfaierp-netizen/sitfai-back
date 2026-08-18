package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NotaCreditoElectronicaTest {

    private NotaCreditoId notaId;
    private EmpresaId empresaId;
    private FacturaId facturaId;
    private Cufe cufeOriginal;
    private MotivoDevolucion motivo;

    @BeforeEach
    void setUp() {
        notaId = new NotaCreditoId(UUID.randomUUID());
        empresaId = new EmpresaId(UUID.randomUUID());
        facturaId = new FacturaId(UUID.randomUUID());
        cufeOriginal = new Cufe("123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456");
        motivo = new MotivoDevolucion("01", "Devolución de parte de los bienes");
    }

    @Test
    void dadoDatosValidos_cuandoGenerarBorrador_entoncesCreaNotaEnBorrador() {
        NotaCreditoElectronica nota = NotaCreditoElectronica.generarBorrador(notaId, empresaId, facturaId, cufeOriginal, motivo);
        
        assertEquals(EstadoFacturaDian.BORRADOR, nota.getEstado());
        assertEquals(Dinero.cero(), nota.getSubtotal());
        assertEquals(Dinero.cero(), nota.getTotalGeneral());
        assertEquals(cufeOriginal, nota.getCufeFacturaAfectada());
        assertNull(nota.getCufe()); // Aún no firmada
    }

    @Test
    void dadoFaltaFacturaO_Cufe_cuandoGenerarBorrador_entoncesLanzaExcepcion() {
        assertThrows(NullPointerException.class, () -> 
                NotaCreditoElectronica.generarBorrador(notaId, empresaId, null, cufeOriginal, motivo)
        );

        assertThrows(NullPointerException.class, () -> 
                NotaCreditoElectronica.generarBorrador(notaId, empresaId, facturaId, null, motivo)
        );
    }

    @Test
    void dadoLineas_cuandoCalcularTotalesReverso_entoncesSumaMatematicaExacta() {
        NotaCreditoElectronica nota = NotaCreditoElectronica.generarBorrador(notaId, empresaId, facturaId, cufeOriginal, motivo);
        
        LineaFactura linea1 = new LineaFactura("Devolución Producto A", new BigDecimal("1"), Dinero.de(50000.00));
        linea1.agregarImpuesto("IVA", new BigDecimal("19"));

        nota.agregarLineaReverso(linea1);
        
        assertEquals(new BigDecimal("50000.00"), nota.getSubtotal().monto());
        assertEquals(new BigDecimal("9500.00"), nota.getTotalImpuestos().monto());
        assertEquals(new BigDecimal("59500.00"), nota.getTotalGeneral().monto());
    }

    @Test
    void dadoNotaLista_cuandoFirmar_entoncesCambiaEstadoYEmiteEvento() {
        NotaCreditoElectronica nota = NotaCreditoElectronica.generarBorrador(notaId, empresaId, facturaId, cufeOriginal, motivo);
        nota.agregarLineaReverso(new LineaFactura("Reverso", new BigDecimal("1"), Dinero.de(100)));
        
        Cufe nuevoCufe = new Cufe("098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765");
        nota.firmar(nuevoCufe);
        
        assertEquals(EstadoFacturaDian.FIRMADA, nota.getEstado());
        assertEquals(1, nota.getDomainEvents().size());
        assertEquals(nuevoCufe, nota.getCufe());
    }

    @Test
    void dadoNotaSinLineas_cuandoFirmar_entoncesLanzaExcepcion() {
        NotaCreditoElectronica nota = NotaCreditoElectronica.generarBorrador(notaId, empresaId, facturaId, cufeOriginal, motivo);
        Cufe nuevoCufe = new Cufe("098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321098765");
        
        assertThrows(DomainException.class, () -> nota.firmar(nuevoCufe));
    }
}
