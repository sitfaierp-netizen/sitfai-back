package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.event.MercanciaRecibidaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class RecepcionMercancia {
    private final EmpresaId empresaId;
    private final RecepcionId id;
    private final OrdenCompraId ordenCompraId;
    private final BodegaId bodegaId;
    private EstadoRecepcion estado;
    private final List<LineaRecepcion> lineas;
    private final List<Object> domainEvents;
    private final Instant fechaCreacion;
    private Instant fechaActualizacion;

    public RecepcionMercancia(EmpresaId empresaId, RecepcionId id, OrdenCompraId ordenCompraId, BodegaId bodegaId, List<LineaRecepcion> lineas) {
        this.empresaId = Objects.requireNonNull(empresaId, "EmpresaId no puede ser nulo.");
        this.id = id != null ? id : RecepcionId.generar();
        this.ordenCompraId = Objects.requireNonNull(ordenCompraId, "OrdenCompraId no puede ser nulo (Regla BOD-04).");
        this.bodegaId = Objects.requireNonNull(bodegaId, "BodegaId no puede ser nulo.");
        this.estado = EstadoRecepcion.PLANIFICADA;
        this.lineas = lineas != null ? new ArrayList<>(lineas) : new ArrayList<>();
        this.domainEvents = new ArrayList<>();
        this.fechaCreacion = Instant.now();
        this.fechaActualizacion = this.fechaCreacion;
    }

    private RecepcionMercancia(EmpresaId empresaId, RecepcionId id, OrdenCompraId ordenCompraId, BodegaId bodegaId, EstadoRecepcion estado, List<LineaRecepcion> lineas) {
        this.empresaId = empresaId;
        this.id = id;
        this.ordenCompraId = ordenCompraId;
        this.bodegaId = bodegaId;
        this.estado = estado;
        this.lineas = lineas != null ? new ArrayList<>(lineas) : new ArrayList<>();
        this.domainEvents = new ArrayList<>();
        this.fechaCreacion = Instant.now();
        this.fechaActualizacion = this.fechaCreacion;
    }

    public static RecepcionMercancia reconstituir(EmpresaId empresaId, RecepcionId id, OrdenCompraId ordenCompraId, BodegaId bodegaId, EstadoRecepcion estado, List<LineaRecepcion> lineas) {
        return new RecepcionMercancia(empresaId, id, ordenCompraId, bodegaId, estado, lineas);
    }

    public EmpresaId getEmpresaId() {
        return empresaId;
    }

    public RecepcionId getId() {
        return id;
    }

    public OrdenCompraId getOrdenCompraId() {
        return ordenCompraId;
    }

    public BodegaId getBodegaId() {
        return bodegaId;
    }

    public EstadoRecepcion getEstado() {
        return estado;
    }

    public List<LineaRecepcion> getLineas() {
        return Collections.unmodifiableList(lineas);
    }

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public Instant getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void recibirProducto(ProductoId productoId, CantidadRecepcion cantidad) {
        if (this.estado == EstadoRecepcion.COMPLETADA) {
            throw new IllegalStateException("No se pueden recibir productos en una recepcion ya completada.");
        }

        Optional<LineaRecepcion> lineaOpt = this.lineas.stream()
                .filter(l -> l.getProductoId().equals(productoId))
                .findFirst();

        if (lineaOpt.isEmpty()) {
            throw new IllegalArgumentException("El producto " + productoId.valor() + " no es parte de la orden de compra.");
        }

        LineaRecepcion linea = lineaOpt.get();
        linea.recibir(cantidad);

        if (this.estado == EstadoRecepcion.PLANIFICADA) {
            this.estado = EstadoRecepcion.EN_PROCESO;
        }
        
        this.fechaActualizacion = Instant.now();
    }

    public void completar() {
        if (this.estado == EstadoRecepcion.COMPLETADA) {
            throw new IllegalStateException("La recepcion ya se encuentra completada.");
        }
        
        // Regla: no requiere que todo est completo, pero s marca el fin del proceso de recepcin
        this.estado = EstadoRecepcion.COMPLETADA;
        this.fechaActualizacion = Instant.now();

        // Registrar evento de dominio
        domainEvents.add(new MercanciaRecibidaEvent(
                this.empresaId,
                this.bodegaId,
                this.id,
                this.getLineas()
        ));
    }

    public java.util.Collection<Object> obtenerEventosDominio() {
        return Collections.unmodifiableList(new ArrayList<>(domainEvents));
    }
}
