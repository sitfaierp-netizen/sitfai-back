package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.event.PackingCompletadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.DireccionEntrega;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.PedidoOrigenId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject.ProductoId;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OrdenDespacho {

    private final DespachoId id;
    private final EmpresaId empresaId;
    private final PedidoOrigenId pedidoOrigenId;
    private final DireccionEntrega direccionEntrega;
    private EstadoDespacho estado;
    private final List<LineaDespacho> lineas;
    private final List<DomainEvent> domainEvents;

    private OrdenDespacho(DespachoId id, EmpresaId empresaId, PedidoOrigenId pedidoOrigenId, DireccionEntrega direccionEntrega, List<LineaDespacho> lineas) {
        this.id = Objects.requireNonNull(id, "DespachoId es obligatorio");
        this.empresaId = Objects.requireNonNull(empresaId, "EmpresaId es obligatorio");
        this.pedidoOrigenId = Objects.requireNonNull(pedidoOrigenId, "PedidoOrigenId es obligatorio");
        this.direccionEntrega = Objects.requireNonNull(direccionEntrega, "DireccionEntrega es obligatoria");
        this.estado = EstadoDespacho.PENDIENTE_PICKING;
        
        if (lineas == null || lineas.isEmpty()) {
            throw new IllegalArgumentException("Una Orden de Despacho debe tener al menos una línea");
        }
        
        this.lineas = new ArrayList<>(lineas);
        this.domainEvents = new ArrayList<>();
    }

    public static OrdenDespacho planificar(EmpresaId empresaId, PedidoOrigenId pedidoOrigenId, DireccionEntrega direccionEntrega, List<LineaDespacho> lineas) {
        return new OrdenDespacho(DespachoId.generar(), empresaId, pedidoOrigenId, direccionEntrega, lineas);
    }

    public void registrarPicking(ProductoId productoId, Cantidad cantidad) {
        Objects.requireNonNull(productoId, "El productoId es obligatorio");
        Objects.requireNonNull(cantidad, "La cantidad es obligatoria");

        if (this.estado != EstadoDespacho.PENDIENTE_PICKING && this.estado != EstadoDespacho.EN_PACKING) {
            throw new IllegalStateException("Solo se puede registrar picking si la orden está PENDIENTE_PICKING o EN_PACKING. Estado actual: " + this.estado);
        }

        Optional<LineaDespacho> lineaOpt = this.lineas.stream()
                .filter(l -> l.getProductoId().equals(productoId))
                .findFirst();

        if (lineaOpt.isEmpty()) {
            throw new IllegalArgumentException("El producto " + productoId.value() + " no pertenece a esta orden de despacho");
        }

        lineaOpt.get().incrementarPreparada(cantidad);
        
        // Si estábamos en PENDIENTE_PICKING y empezamos a registrar, pasamos a EN_PACKING
        if (this.estado == EstadoDespacho.PENDIENTE_PICKING) {
            this.estado = EstadoDespacho.EN_PACKING;
        }
    }

    public void completarPacking() {
        boolean todasCompletadas = this.lineas.stream().allMatch(LineaDespacho::estaCompletada);
        
        if (!todasCompletadas) {
            throw new IllegalStateException("No se puede completar el packing. Hay líneas con cantidades preparadas incompletas.");
        }
        
        this.estado = EstadoDespacho.LISTO_PARA_ENVIO;
        
        this.domainEvents.add(PackingCompletadoEvent.of(this.id, this.empresaId, this.pedidoOrigenId));
    }

    // Getters y manejo de eventos
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(this.domainEvents);
    }
    
    public List<DomainEvent> drainDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }

    public DespachoId getId() {
        return id;
    }

    public EmpresaId getEmpresaId() {
        return empresaId;
    }

    public PedidoOrigenId getPedidoOrigenId() {
        return pedidoOrigenId;
    }

    public DireccionEntrega getDireccionEntrega() {
        return direccionEntrega;
    }

    public EstadoDespacho getEstado() {
        return estado;
    }

    public List<LineaDespacho> getLineas() {
        return Collections.unmodifiableList(lineas);
    }
}
