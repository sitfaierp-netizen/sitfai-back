package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.FacturaEmitidaEvent;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Impuesto;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo.Ruc;
import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.DocumentoTransaccional;
import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.enums.DocumentStatus;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception.DocumentStateException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Factura implements DocumentoTransaccional {
    private final FacturaId id;
    private final UUID empresaId;
    private final ClienteId clienteId;
    private final PedidoId pedidoId; // Referencia opcional/obligatoria al pedido
    private final Ruc rucCliente;
    
    private final List<LineaFactura> lineas;
    private final List<Impuesto> impuestos;

    private Dinero subtotal;
    private Dinero totalImpuestos;
    private Dinero totalGeneral;

    private DocumentStatus estado;
    private final Long version;
    private final Instant createdAt;
    private final String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Factory method para creación
    public static Factura crear(FacturaId id, UUID empresaId, ClienteId clienteId, PedidoId pedidoId, Ruc rucCliente, String createdBy) {
        return new Factura(
                id, 
                empresaId, 
                clienteId, 
                pedidoId, 
                rucCliente, 
                new ArrayList<>(), 
                new ArrayList<>(), 
                Dinero.cero(),
                Dinero.cero(),
                Dinero.cero(),
                DocumentStatus.BORRADOR,
                0L,
                Instant.now(),
                createdBy,
                null,
                null
        );
    }

    // Constructor completo privado
    private Factura(FacturaId id, UUID empresaId, ClienteId clienteId, PedidoId pedidoId, Ruc rucCliente,
                    List<LineaFactura> lineas, List<Impuesto> impuestos, Dinero subtotal, Dinero totalImpuestos, Dinero totalGeneral,
                    DocumentStatus estado, Long version, Instant createdAt, String createdBy, Instant updatedAt, String updatedBy) {
        if (empresaId == null) throw new IllegalArgumentException("empresaId es obligatorio");
        this.id = id;
        this.empresaId = empresaId;
        this.clienteId = clienteId;
        this.pedidoId = pedidoId;
        this.rucCliente = rucCliente;
        this.lineas = lineas;
        this.impuestos = impuestos;
        this.subtotal = subtotal;
        this.totalImpuestos = totalImpuestos;
        this.totalGeneral = totalGeneral;
        this.estado = estado;
        this.version = version;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public void agregarLinea(String concepto, BigDecimal cantidad, Dinero precioUnitario) {
        if (!puedeEditar()) {
            throw new DocumentStateException("No se pueden agregar líneas a una factura que no está en BORRADOR.");
        }
        LineaFactura linea = new LineaFactura(concepto, cantidad, precioUnitario);
        this.lineas.add(linea);
        recalcularTotales();
    }

    public void aplicarImpuesto(String nombre, BigDecimal porcentaje) {
        if (!puedeEditar()) {
            throw new DocumentStateException("No se pueden aplicar impuestos a una factura que no está en BORRADOR.");
        }
        Impuesto impuesto = Impuesto.calcular(nombre, porcentaje, this.subtotal);
        this.impuestos.add(impuesto);
        recalcularTotales();
    }

    private void recalcularTotales() {
        this.subtotal = lineas.stream()
                .map(LineaFactura::calcularSubtotal)
                .reduce(Dinero.cero(), Dinero::sumar);

        // Recalcular impuestos basados en el nuevo subtotal
        List<Impuesto> impuestosRecalculados = new ArrayList<>();
        for (Impuesto imp : this.impuestos) {
            impuestosRecalculados.add(Impuesto.calcular(imp.tipoImpuesto(), imp.tarifa(), this.subtotal));
        }
        this.impuestos.clear();
        this.impuestos.addAll(impuestosRecalculados);

        this.totalImpuestos = impuestos.stream()
                .map(Impuesto::valor)
                .reduce(Dinero.cero(), Dinero::sumar);

        this.totalGeneral = this.subtotal.sumar(this.totalImpuestos);
    }

    @Override
    public void emitir() {
        DocumentoTransaccional.super.emitir();
        // Domain Event: MT-01 (empresaId presente)
        this.domainEvents.add(new FacturaEmitidaEvent(this.id.value(), this.empresaId, this.totalGeneral.monto()));
    }

    @Override
    public void cambiarEstado(DocumentStatus nuevoEstado) {
        this.estado = nuevoEstado;
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    // Getters
    public FacturaId getId() { return id; }
    public UUID getEmpresaId() { return empresaId; }
    public ClienteId getClienteId() { return clienteId; }
    public PedidoId getPedidoId() { return pedidoId; }
    public Ruc getRucCliente() { return rucCliente; }
    public List<LineaFactura> getLineas() { return Collections.unmodifiableList(lineas); }
    public List<Impuesto> getImpuestos() { return Collections.unmodifiableList(impuestos); }
    public Dinero getSubtotal() { return subtotal; }
    public Dinero getTotalImpuestos() { return totalImpuestos; }
    public Dinero getTotalGeneral() { return totalGeneral; }
    @Override public DocumentStatus getEstado() { return estado; }
    public Long getVersion() { return version; }
    @Override public Instant getCreatedAt() { return createdAt; }
    @Override public String getCreatedBy() { return createdBy; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
}
