package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.event.RecetaProduccionAprobadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.domain.model.bom.vo.*;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ListaMateriales extends AbstractAggregateRoot<ListaMateriales> {

    private final EmpresaId empresaId;
    private final RecetaId id;
    private final ProductoFinalId productoFinalId;
    private EstadoReceta estado;
    private final List<ComponenteReceta> componentes;
    private Instant fechaCreacion;
    private Instant fechaActualizacion;

    public ListaMateriales(EmpresaId empresaId, RecetaId id, ProductoFinalId productoFinalId) {
        this.empresaId = Objects.requireNonNull(empresaId, "El empresaId no puede ser nulo");
        this.id = Objects.requireNonNull(id, "El id no puede ser nulo");
        this.productoFinalId = Objects.requireNonNull(productoFinalId, "El productoFinalId no puede ser nulo");
        this.estado = EstadoReceta.BORRADOR;
        this.componentes = new ArrayList<>();
        this.fechaCreacion = Instant.now();
        this.fechaActualizacion = this.fechaCreacion;
    }

    private ListaMateriales(EmpresaId empresaId, RecetaId id, ProductoFinalId productoFinalId, EstadoReceta estado, List<ComponenteReceta> componentes) {
        this.empresaId = Objects.requireNonNull(empresaId, "El empresaId no puede ser nulo");
        this.id = Objects.requireNonNull(id, "El id no puede ser nulo");
        this.productoFinalId = Objects.requireNonNull(productoFinalId, "El productoFinalId no puede ser nulo");
        this.estado = Objects.requireNonNull(estado, "El estado no puede ser nulo");
        this.componentes = new ArrayList<>(componentes);
        this.fechaCreacion = Instant.now();
        this.fechaActualizacion = this.fechaCreacion;
    }

    public static ListaMateriales reconstituir(EmpresaId empresaId, RecetaId id, ProductoFinalId productoFinalId, EstadoReceta estado, List<ComponenteReceta> componentes) {
        return new ListaMateriales(empresaId, id, productoFinalId, estado, componentes);
    }

    public void agregarComponente(InsumoId insumoId, CantidadInsumo cantidad) {
        if (this.estado != EstadoReceta.BORRADOR) {
            throw new IllegalStateException("Solo se pueden agregar componentes si la receta est en estado BORRADOR");
        }
        
        // Verificar si ya existe para evitar duplicados, si existe podramos sumar la cantidad o rechazar
        boolean existe = this.componentes.stream()
                .anyMatch(c -> c.getInsumoId().valor().equals(insumoId.valor()));
        if (existe) {
            throw new IllegalStateException("El insumo ya existe en la lista de materiales");
        }

        this.componentes.add(new ComponenteReceta(insumoId, cantidad));
        this.fechaActualizacion = Instant.now();
    }

    public void removerComponente(InsumoId insumoId) {
        if (this.estado != EstadoReceta.BORRADOR) {
            throw new IllegalStateException("Solo se pueden remover componentes si la receta est en estado BORRADOR");
        }

        boolean removido = this.componentes.removeIf(c -> c.getInsumoId().valor().equals(insumoId.valor()));
        if (removido) {
            this.fechaActualizacion = Instant.now();
        }
    }

    public void aprobar() {
        if (this.estado != EstadoReceta.BORRADOR) {
            throw new IllegalStateException("La receta ya fue aprobada o est obsoleta");
        }
        if (this.componentes.isEmpty()) {
            throw new IllegalStateException("No se puede aprobar una receta sin componentes");
        }

        this.estado = EstadoReceta.APROBADA;
        this.fechaActualizacion = Instant.now();

        // Registrar evento de dominio
        registerEvent(new RecetaProduccionAprobadaEvent(this.empresaId, this.id, this.productoFinalId));
    }

    public void marcarComoObsoleta() {
        this.estado = EstadoReceta.OBSOLETA;
        this.fechaActualizacion = Instant.now();
    }

    public EmpresaId getEmpresaId() { return empresaId; }
    public RecetaId getId() { return id; }
    public ProductoFinalId getProductoFinalId() { return productoFinalId; }
    public EstadoReceta getEstado() { return estado; }
    public List<ComponenteReceta> getComponentes() { return Collections.unmodifiableList(componentes); }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public Instant getFechaActualizacion() { return fechaActualizacion; }

    public java.util.Collection<Object> obtenerEventosDominio() {
        return domainEvents();
    }
}
