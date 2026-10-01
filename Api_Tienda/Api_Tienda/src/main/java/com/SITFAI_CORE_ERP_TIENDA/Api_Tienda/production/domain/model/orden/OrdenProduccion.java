package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.event.ProduccionCompletadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.event.ProduccionIniciadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.orden.vo.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class OrdenProduccion {

    private final EmpresaId empresaId;
    private final OrdenProduccionId id;
    private final RecetaId recetaId;
    private final BodegaId bodegaId;
    private final CantidadProducir cantidadProducir;
    private EstadoOrdenProduccion estado;
    private final List<Object> domainEvents;
    private Instant fechaCreacion;
    private Instant fechaActualizacion;

    public OrdenProduccion(EmpresaId empresaId, OrdenProduccionId id, RecetaId recetaId, BodegaId bodegaId, CantidadProducir cantidadProducir) {
        this.empresaId = Objects.requireNonNull(empresaId, "El empresaId no puede ser nulo");
        this.id = Objects.requireNonNull(id, "El id no puede ser nulo");
        this.recetaId = Objects.requireNonNull(recetaId, "El recetaId no puede ser nulo");
        this.bodegaId = Objects.requireNonNull(bodegaId, "El bodegaId no puede ser nulo");
        this.cantidadProducir = Objects.requireNonNull(cantidadProducir, "La cantidad a producir no puede ser nula");
        this.estado = EstadoOrdenProduccion.PLANIFICADA;
        this.domainEvents = new ArrayList<>();
        this.fechaCreacion = Instant.now();
        this.fechaActualizacion = this.fechaCreacion;
    }

    private OrdenProduccion(EmpresaId empresaId, OrdenProduccionId id, RecetaId recetaId, BodegaId bodegaId, CantidadProducir cantidadProducir, EstadoOrdenProduccion estado) {
        this.empresaId = Objects.requireNonNull(empresaId, "El empresaId no puede ser nulo");
        this.id = Objects.requireNonNull(id, "El id no puede ser nulo");
        this.recetaId = Objects.requireNonNull(recetaId, "El recetaId no puede ser nulo");
        this.bodegaId = Objects.requireNonNull(bodegaId, "El bodegaId no puede ser nulo");
        this.cantidadProducir = Objects.requireNonNull(cantidadProducir, "La cantidad a producir no puede ser nula");
        this.estado = Objects.requireNonNull(estado, "El estado no puede ser nulo");
        this.domainEvents = new ArrayList<>();
        this.fechaCreacion = Instant.now();
        this.fechaActualizacion = this.fechaCreacion;
    }

    public static OrdenProduccion reconstituir(EmpresaId empresaId, OrdenProduccionId id, RecetaId recetaId, BodegaId bodegaId, CantidadProducir cantidadProducir, EstadoOrdenProduccion estado) {
        return new OrdenProduccion(empresaId, id, recetaId, bodegaId, cantidadProducir, estado);
    }

    public void iniciarProduccion() {
        if (this.estado != EstadoOrdenProduccion.PLANIFICADA) {
            throw new IllegalStateException("Solo se puede iniciar una orden que est en estado PLANIFICADA");
        }

        this.estado = EstadoOrdenProduccion.EN_PROGRESO;
        this.fechaActualizacion = Instant.now();

        domainEvents.add(new ProduccionIniciadaEvent(this.empresaId, this.id, this.recetaId));
    }

    public void completarProduccion() {
        if (this.estado != EstadoOrdenProduccion.EN_PROGRESO) {
            throw new IllegalStateException("Solo se puede completar una orden que est en estado EN_PROGRESO");
        }

        this.estado = EstadoOrdenProduccion.COMPLETADA;
        this.fechaActualizacion = Instant.now();

        domainEvents.add(new ProduccionCompletadaEvent(this.empresaId, this.id, this.recetaId, this.cantidadProducir));
    }

    public void cancelarProduccion() {
        if (this.estado == EstadoOrdenProduccion.COMPLETADA) {
            throw new IllegalStateException("No se puede cancelar una orden que ya fue COMPLETADA");
        }
        if (this.estado == EstadoOrdenProduccion.CANCELADA) {
            throw new IllegalStateException("La orden ya est CANCELADA");
        }

        this.estado = EstadoOrdenProduccion.CANCELADA;
        this.fechaActualizacion = Instant.now();
    }

    public EmpresaId getEmpresaId() { return empresaId; }
    public OrdenProduccionId getId() { return id; }
    public RecetaId getRecetaId() { return recetaId; }
    public BodegaId getBodegaId() { return bodegaId; }
    public CantidadProducir getCantidadProducir() { return cantidadProducir; }
    public EstadoOrdenProduccion getEstado() { return estado; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public Instant getFechaActualizacion() { return fechaActualizacion; }

    public java.util.Collection<Object> obtenerEventosDominio() {
        return Collections.unmodifiableList(new ArrayList<>(domainEvents));
    }

    public void limpiarEventosDominio() {
        domainEvents.clear();
    }
}
