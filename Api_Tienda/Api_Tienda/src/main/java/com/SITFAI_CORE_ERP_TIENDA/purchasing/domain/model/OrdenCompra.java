package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event.OrdenCompraRecibidaEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.ProveedorId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Aggregate Root: Orden de Compra.
 * Aislado de todo framework (Regla 1 y 3).
 */
public class OrdenCompra {

    private final OrdenCompraId id;
    private final EmpresaId empresaId;
    private final ProveedorId proveedorId;
    private final Instant fechaCreacion;
    
    private EstadoOrden estado;
    private final List<LineaOrdenCompra> lineas;
    private Dinero costoTotal;
    
    private final List<DomainEvent> domainEvents;

    private OrdenCompra(OrdenCompraId id, EmpresaId empresaId, ProveedorId proveedorId) {
        this.id = Objects.requireNonNull(id, "El ID de OrdenCompra no puede ser nulo");
        this.empresaId = Objects.requireNonNull(empresaId, "El EmpresaId no puede ser nulo");
        this.proveedorId = Objects.requireNonNull(proveedorId, "El ProveedorId no puede ser nulo");
        this.fechaCreacion = Instant.now();
        this.estado = EstadoOrden.BORRADOR;
        this.lineas = new ArrayList<>();
        this.costoTotal = Dinero.cero();
        this.domainEvents = new ArrayList<>();
    }

    /**
     * Factory method para iniciar una Orden de Compra.
     */
    public static OrdenCompra crearBorrador(OrdenCompraId id, EmpresaId empresaId, ProveedorId proveedorId) {
        return new OrdenCompra(id, empresaId, proveedorId);
    }

    /**
     * Agrega una línea y recalcula el costo total.
     */
    public void agregarLinea(LineaOrdenCompra linea) {
        if (this.estado != EstadoOrden.BORRADOR) {
            throw new DomainException("No se pueden modificar líneas de una orden en estado " + this.estado);
        }
        this.lineas.add(Objects.requireNonNull(linea));
        recalcularCostoTotal();
    }

    private void recalcularCostoTotal() {
        this.costoTotal = lineas.stream()
                .map(LineaOrdenCompra::calcularSubtotal)
                .reduce(Dinero.cero(), Dinero::sumar);
    }

    /**
     * Transiciona a estado EMITIDA.
     */
    public void emitir() {
        if (this.estado != EstadoOrden.BORRADOR) {
            throw new DomainException("Solo se puede emitir una orden en estado BORRADOR.");
        }
        if (this.lineas.isEmpty()) {
            throw new DomainException("No se puede emitir una orden vacía.");
        }
        if (this.costoTotal.monto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("No se puede emitir una orden con costo total cero.");
        }
        this.estado = EstadoOrden.EMITIDA;
    }

    /**
     * Transiciona a estado RECIBIDA y emite el evento de dominio.
     */
    public void marcarComoRecibida() {
        if (this.estado != EstadoOrden.EMITIDA) {
            throw new DomainException("Solo una orden EMITIDA puede ser marcada como RECIBIDA.");
        }
        this.estado = EstadoOrden.RECIBIDA;

        List<OrdenCompraRecibidaEvent.LineaRecibida> lineasRecibidas = this.lineas.stream()
                .map(l -> new OrdenCompraRecibidaEvent.LineaRecibida(l.getProductoId().valor(), l.getCantidadSolicitada()))
                .collect(Collectors.toList());

        this.domainEvents.add(OrdenCompraRecibidaEvent.of(this.empresaId, this.id, lineasRecibidas));
    }

    /**
     * Transiciona a estado CANCELADA.
     */
    public void cancelar() {
        if (this.estado == EstadoOrden.RECIBIDA) {
            throw new DomainException("No se puede cancelar una orden que ya fue RECIBIDA.");
        }
        this.estado = EstadoOrden.CANCELADA;
    }

    // Getters
    public OrdenCompraId getId() { return id; }
    public EmpresaId getEmpresaId() { return empresaId; }
    public ProveedorId getProveedorId() { return proveedorId; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public EstadoOrden getEstado() { return estado; }
    public List<LineaOrdenCompra> getLineas() { return Collections.unmodifiableList(lineas); }
    public Dinero getCostoTotal() { return costoTotal; }
    public List<DomainEvent> getDomainEvents() { return Collections.unmodifiableList(domainEvents); }
}
