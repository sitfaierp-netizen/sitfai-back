package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.NotaCreditoFirmadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception.BillingRuleException;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root: Nota de Crédito Electrónica (DIAN).
 * Cero frameworks, Java puro.
 */
public class NotaCreditoElectronica {
    
    private final NotaCreditoId id;
    private final EmpresaId empresaId;
    
    // Referencia obligatoria a la factura afectada (Invariante DIAN)
    private final FacturaId facturaAfectadaId;
    private final Cufe cufeFacturaAfectada;
    private final MotivoDevolucion motivo;
    
    private Cufe cufe; // El CUFE propio de la Nota de Crédito (asignado al firmar)
    private EstadoFacturaDian estado;
    
    private final List<LineaFactura> lineasReversadas;
    private final List<DomainEvent> domainEvents;

    private Dinero subtotal;
    private Dinero totalImpuestos;
    private Dinero totalGeneral;

    private NotaCreditoElectronica(NotaCreditoId id, EmpresaId empresaId, FacturaId facturaAfectadaId, 
                                   Cufe cufeFacturaAfectada, MotivoDevolucion motivo) {
        this.id = Objects.requireNonNull(id, "ID no puede ser nulo");
        this.empresaId = Objects.requireNonNull(empresaId, "EmpresaId no puede ser nulo");
        this.facturaAfectadaId = Objects.requireNonNull(facturaAfectadaId, "El ID de la Factura afectada es obligatorio");
        this.cufeFacturaAfectada = Objects.requireNonNull(cufeFacturaAfectada, "El CUFE de la Factura afectada es obligatorio (Invariante DIAN)");
        this.motivo = Objects.requireNonNull(motivo, "El motivo de devolución es obligatorio");
        
        this.estado = EstadoFacturaDian.BORRADOR;
        this.lineasReversadas = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
        
        this.subtotal = Dinero.cero();
        this.totalImpuestos = Dinero.cero();
        this.totalGeneral = Dinero.cero();
    }

    /**
     * Factory Method: Crea una Nota de Crédito preparatoria.
     * Invariante DIAN: Una nota de crédito huérfana es ilegal (exige FacturaId y Cufe originales).
     */
    public static NotaCreditoElectronica generarBorrador(NotaCreditoId id, EmpresaId empresaId, 
                                                         FacturaId facturaAfectadaId, Cufe cufeFacturaAfectada, 
                                                         MotivoDevolucion motivo) {
        return new NotaCreditoElectronica(id, empresaId, facturaAfectadaId, cufeFacturaAfectada, motivo);
    }

    public void agregarLineaReverso(LineaFactura linea) {
        if (estado != EstadoFacturaDian.BORRADOR) {
            throw new BillingRuleException("Solo se pueden agregar líneas en estado BORRADOR.");
        }
        lineasReversadas.add(linea);
        calcularTotalesReverso();
    }

    /**
     * Sumariza los montos a favor del cliente.
     */
    public void calcularTotalesReverso() {
        this.subtotal = lineasReversadas.stream()
                .map(LineaFactura::calcularSubtotal)
                .reduce(Dinero.cero(), Dinero::sumar);

        this.totalImpuestos = lineasReversadas.stream()
                .map(LineaFactura::calcularTotalImpuestos)
                .reduce(Dinero.cero(), Dinero::sumar);

        this.totalGeneral = this.subtotal.sumar(this.totalImpuestos);
    }

    /**
     * Transiciona el estado a FIRMADA asignando la huella criptográfica.
     */
    public void firmar(Cufe cufeNotaCredito) {
        if (lineasReversadas.isEmpty()) {
            throw new BillingRuleException("No se puede firmar una Nota de Crédito sin líneas a reversar.");
        }
        if (estado != EstadoFacturaDian.BORRADOR) {
            throw new BillingRuleException("La Nota de Crédito ya se encuentra en un estado que impide su firma: " + estado);
        }
        
        this.cufe = cufeNotaCredito;
        this.estado = EstadoFacturaDian.FIRMADA;
        
        this.domainEvents.add(NotaCreditoFirmadaEvent.of(this.id, this.cufe));
    }

    // Getters
    public NotaCreditoId getId() { return id; }
    public EmpresaId getEmpresaId() { return empresaId; }
    public FacturaId getFacturaAfectadaId() { return facturaAfectadaId; }
    public Cufe getCufeFacturaAfectada() { return cufeFacturaAfectada; }
    public MotivoDevolucion getMotivo() { return motivo; }
    public Cufe getCufe() { return cufe; }
    public EstadoFacturaDian getEstado() { return estado; }
    public List<LineaFactura> getLineasReversadas() { return Collections.unmodifiableList(lineasReversadas); }
    public List<DomainEvent> getDomainEvents() { return Collections.unmodifiableList(domainEvents); }
    public Dinero getSubtotal() { return subtotal; }
    public Dinero getTotalImpuestos() { return totalImpuestos; }
    public Dinero getTotalGeneral() { return totalGeneral; }
}
