package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.DocumentoTransaccional;
import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.enums.DocumentStatus;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event.OrdenCompraCreadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event.OrdenCompraEmitidaEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.vo.ProveedorId;
import com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception.DocumentStateException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class OrdenCompra implements DocumentoTransaccional {

    private final OrdenCompraId id;
    private final UUID empresaId;
    private final ProveedorId proveedorId;
    private final UUID bodegaDestinoId;
    
    private final List<LineaOrdenCompra> lineas;
    private Dinero totalMonetario;
    
    private DocumentStatus estado;
    private Long version;
    
    private final Instant createdAt;
    private final String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private OrdenCompra(OrdenCompraId id, UUID empresaId, ProveedorId proveedorId, UUID bodegaDestinoId, String createdBy) {
        this.id = Objects.requireNonNull(id, "El ID de OrdenCompra no puede ser nulo.");
        this.empresaId = Objects.requireNonNull(empresaId, "El empresaId no puede ser nulo.");
        this.proveedorId = Objects.requireNonNull(proveedorId, "El proveedorId no puede ser nulo.");
        this.bodegaDestinoId = Objects.requireNonNull(bodegaDestinoId, "La bodegaDestinoId no puede ser nula.");
        this.lineas = new ArrayList<>();
        this.totalMonetario = Dinero.CERO;
        this.estado = DocumentStatus.BORRADOR;
        this.version = 0L;
        this.createdAt = Instant.now();
        this.createdBy = Objects.requireNonNull(createdBy, "El autor de creación no puede ser nulo.");
        this.updatedAt = this.createdAt;
        this.updatedBy = this.createdBy;
    }

    public static OrdenCompra crear(OrdenCompraId id, UUID empresaId, ProveedorId proveedorId, UUID bodegaDestinoId, String createdBy) {
        OrdenCompra orden = new OrdenCompra(id, empresaId, proveedorId, bodegaDestinoId, createdBy);
        orden.addDomainEvent(OrdenCompraCreadaEvent.ahora(empresaId, id));
        return orden;
    }

    public void agregarLinea(LineaOrdenCompra linea) {
        if (!puedeEditar()) {
            throw new DocumentStateException("No se pueden agregar líneas a un documento que no está en BORRADOR.");
        }
        Objects.requireNonNull(linea, "La línea no puede ser nula.");
        this.lineas.add(linea);
        recalcularTotal();
        actualizarAuditoria();
    }

    private void recalcularTotal() {
        this.totalMonetario = this.lineas.stream()
                .map(LineaOrdenCompra::getSubtotal)
                .reduce(Dinero.CERO, Dinero::sumar);
    }

    @Override
    public DocumentStatus getEstado() {
        return this.estado;
    }

    @Override
    public Instant getCreatedAt() {
        return this.createdAt;
    }

    @Override
    public String getCreatedBy() {
        return this.createdBy;
    }

    @Override
    public void cambiarEstado(DocumentStatus nuevoEstado) {
        this.estado = nuevoEstado;
        actualizarAuditoria();
    }

    @Override
    public void emitir() {
        if (this.lineas.isEmpty()) {
            throw new DocumentStateException("No se puede emitir una Orden de Compra sin líneas.");
        }
        DocumentoTransaccional.super.emitir();
        addDomainEvent(OrdenCompraEmitidaEvent.ahora(this.empresaId, this.id));
    }

    public void aprobar() {
        if (this.estado != DocumentStatus.EMITIDO) {
            throw new DocumentStateException("Sólo se puede aprobar una orden EMITIDA.");
        }
        this.estado = DocumentStatus.APROBADO;
        actualizarAuditoria();
    }

    public void recibir() {
        if (this.estado != DocumentStatus.APROBADO) {
            throw new DocumentStateException("Sólo se puede recibir una orden APROBADA.");
        }
        this.estado = DocumentStatus.RECIBIDO;
        actualizarAuditoria();
    }

    private void actualizarAuditoria() {
        this.updatedAt = Instant.now();
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    private void addDomainEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return events;
    }

    public OrdenCompraId getId() { return id; }
    public UUID getEmpresaId() { return empresaId; }
    public ProveedorId getProveedorId() { return proveedorId; }
    public UUID getBodegaDestinoId() { return bodegaDestinoId; }
    public List<LineaOrdenCompra> getLineas() { return Collections.unmodifiableList(lineas); }
    public Dinero getTotalMonetario() { return totalMonetario; }
    public Long getVersion() { return version; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
}
