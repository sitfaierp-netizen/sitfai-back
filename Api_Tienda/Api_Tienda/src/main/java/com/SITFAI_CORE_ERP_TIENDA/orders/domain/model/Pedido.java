package com.SITFAI_CORE_ERP_TIENDA.orders.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.DocumentoTransaccional;
import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.enums.DocumentStatus;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.event.PedidoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.event.PedidoCreadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.orders.domain.model.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception.DocumentStateException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Pedido implements DocumentoTransaccional {

    private final PedidoId id;
    private final UUID empresaId;
    private final ClienteId clienteId;

    private final List<LineaPedido> lineas;
    private Dinero totalMonetario;

    private DocumentStatus estado;
    private Long version;
    
    // Auditoría
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;

    private final List<DomainEvent> domainEvents;

    // Factory method (Create)
    public static Pedido crear(PedidoId id, UUID empresaId, ClienteId clienteId, String createdBy) {
        return new Pedido(id, empresaId, clienteId, createdBy);
    }

    private Pedido(PedidoId id, UUID empresaId, ClienteId clienteId, String createdBy) {
        if (id == null) throw new IllegalArgumentException("El ID del pedido no puede ser nulo");
        if (empresaId == null) throw new IllegalArgumentException("El ID de la empresa no puede ser nulo");
        if (clienteId == null) throw new IllegalArgumentException("El ID del cliente no puede ser nulo");

        this.id = id;
        this.empresaId = empresaId;
        this.clienteId = clienteId;
        this.lineas = new ArrayList<>();
        this.totalMonetario = Dinero.zero();
        
        this.estado = DocumentStatus.BORRADOR;
        this.version = 0L;
        
        this.createdAt = Instant.now();
        this.createdBy = createdBy != null ? createdBy : "system";
        
        this.domainEvents = new ArrayList<>();
        this.domainEvents.add(new PedidoCreadoEvent(this.id, this.empresaId));
    }

    // Reconstitution Constructor (for persistence layer)
    public Pedido(PedidoId id, UUID empresaId, ClienteId clienteId, List<LineaPedido> lineas, Dinero totalMonetario, 
                  DocumentStatus estado, Long version, Instant createdAt, String createdBy, Instant updatedAt, String updatedBy) {
        this.id = id;
        this.empresaId = empresaId;
        this.clienteId = clienteId;
        this.lineas = new ArrayList<>(lineas != null ? lineas : Collections.emptyList());
        this.totalMonetario = totalMonetario != null ? totalMonetario : Dinero.zero();
        this.estado = estado;
        this.version = version;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
        this.domainEvents = new ArrayList<>();
    }

    public void agregarLinea(ProductoId productoId, int cantidad, Dinero precioUnitario) {
        if (!puedeEditar()) {
            throw new DocumentStateException("No se puede modificar un pedido que no está en borrador");
        }
        
        LineaPedido nuevaLinea = new LineaPedido(productoId, cantidad, precioUnitario);
        this.lineas.add(nuevaLinea);
        recalcularTotal();
    }
    
    public void eliminarLinea(UUID lineaId) {
        if (!puedeEditar()) {
            throw new DocumentStateException("No se puede modificar un pedido que no está en borrador");
        }
        
        boolean removed = this.lineas.removeIf(l -> l.getId().equals(lineaId));
        if (removed) {
            recalcularTotal();
        }
    }

    private void recalcularTotal() {
        Dinero nuevoTotal = Dinero.zero();
        for (LineaPedido linea : this.lineas) {
            nuevoTotal = nuevoTotal.sumar(linea.getSubtotal());
        }
        this.totalMonetario = nuevoTotal;
    }

    // Comportamientos de Dominio
    
    public void confirmar() {
        if (this.lineas.isEmpty()) {
            throw new DocumentStateException("No se puede confirmar un pedido sin líneas");
        }
        emitir(); // de DocumentoTransaccional
        this.domainEvents.add(new PedidoConfirmadoEvent(this.id, this.empresaId));
    }

    @Override
    public void cambiarEstado(DocumentStatus nuevoEstado) {
        // Validación extra de estado si fuera necesaria
        this.estado = nuevoEstado;
    }

    // Getters
    public PedidoId getId() { return id; }
    public UUID getEmpresaId() { return empresaId; }
    public ClienteId getClienteId() { return clienteId; }
    public List<LineaPedido> getLineas() { return Collections.unmodifiableList(lineas); }
    public Dinero getTotalMonetario() { return totalMonetario; }
    
    @Override
    public DocumentStatus getEstado() { return estado; }
    public Long getVersion() { return version; }
    @Override
    public Instant getCreatedAt() { return createdAt; }
    @Override
    public String getCreatedBy() { return createdBy; }
    public Instant getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(id, pedido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
