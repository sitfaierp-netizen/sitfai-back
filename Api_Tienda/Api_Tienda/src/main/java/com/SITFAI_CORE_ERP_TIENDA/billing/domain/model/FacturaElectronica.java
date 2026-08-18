package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.FacturaAnuladaEvent;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.FacturaFirmadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception.BillingRuleException;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root: Factura Electrónica (Colombia / DIAN).
 * Cero frameworks, Java puro.
 */
public class FacturaElectronica {
    private final FacturaId id;
    private final EmpresaId empresaId;
    private final Nit nitEmisor;
    private final Nit nitReceptor;
    private final ResolucionDian resolucionDian;
    private Cufe cufe;
    private EstadoFacturaDian estado;
    
    private final List<LineaFactura> lineas;
    private final List<DomainEvent> domainEvents;

    private Dinero subtotal;
    private Dinero totalImpuestos;
    private Dinero totalGeneral;

    private FacturaElectronica(FacturaId id, EmpresaId empresaId, Nit nitEmisor, Nit nitReceptor, ResolucionDian resolucionDian) {
        this.id = id;
        this.empresaId = empresaId;
        this.nitEmisor = nitEmisor;
        this.nitReceptor = nitReceptor;
        this.resolucionDian = resolucionDian;
        this.estado = EstadoFacturaDian.BORRADOR;
        this.lineas = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
        this.subtotal = Dinero.cero();
        this.totalImpuestos = Dinero.cero();
        this.totalGeneral = Dinero.cero();
    }

    /**
     * Factory Method: Crea una factura preparatoria.
     */
    public static FacturaElectronica generarBorrador(FacturaId id, EmpresaId empresaId, Nit nitEmisor, Nit nitReceptor, ResolucionDian resolucionDian) {
        return new FacturaElectronica(id, empresaId, nitEmisor, nitReceptor, resolucionDian);
    }

    public void agregarLinea(LineaFactura linea) {
        if (estado != EstadoFacturaDian.BORRADOR) {
            throw new BillingRuleException("Solo se pueden agregar líneas en estado BORRADOR.");
        }
        lineas.add(linea);
        calcularTotales();
    }

    /**
     * Sumariza subtotales e impuestos de todas las líneas.
     */
    public void calcularTotales() {
        this.subtotal = lineas.stream()
                .map(LineaFactura::calcularSubtotal)
                .reduce(Dinero.cero(), Dinero::sumar);

        this.totalImpuestos = lineas.stream()
                .map(LineaFactura::calcularTotalImpuestos)
                .reduce(Dinero.cero(), Dinero::sumar);

        this.totalGeneral = this.subtotal.sumar(this.totalImpuestos);
    }

    /**
     * Transiciona el estado a FIRMADA asignando la huella criptográfica.
     */
    public void firmar(Cufe cufeAsignado) {
        if (lineas.isEmpty()) {
            throw new BillingRuleException("No se puede firmar una factura sin líneas de detalle.");
        }
        if (resolucionDian.estaExpirada()) {
            throw new BillingRuleException("La resolución DIAN asociada a esta factura ha expirado.");
        }
        if (estado != EstadoFacturaDian.BORRADOR) {
            throw new BillingRuleException("La factura ya se encuentra en un estado que impide su firma: " + estado);
        }
        
        this.cufe = cufeAsignado;
        this.estado = EstadoFacturaDian.FIRMADA;
        
        this.domainEvents.add(FacturaFirmadaEvent.of(this.id, this.cufe));
    }

    /**
     * Anula la factura, aplicando la regla AUD-04 (inmutabilidad financiera).
     */
    public void anular(String motivo) {
        if (estado == EstadoFacturaDian.ANULADA) {
            throw new BillingRuleException("No se puede anular una factura previamente anulada.");
        }
        this.estado = EstadoFacturaDian.ANULADA;
        this.domainEvents.add(FacturaAnuladaEvent.of(this.id, this.empresaId, motivo));
    }

    // Getters
    public FacturaId getId() { return id; }
    public EmpresaId getEmpresaId() { return empresaId; }
    public Nit getNitEmisor() { return nitEmisor; }
    public Nit getNitReceptor() { return nitReceptor; }
    public ResolucionDian getResolucionDian() { return resolucionDian; }
    public Cufe getCufe() { return cufe; }
    public EstadoFacturaDian getEstado() { return estado; }
    public List<LineaFactura> getLineas() { return Collections.unmodifiableList(lineas); }
    public List<DomainEvent> getDomainEvents() { return Collections.unmodifiableList(domainEvents); }
    public Dinero getSubtotal() { return subtotal; }
    public Dinero getTotalImpuestos() { return totalImpuestos; }
    public Dinero getTotalGeneral() { return totalGeneral; }
}
