package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho;

import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.event.DespachoCompletadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.EstadoDespacho;
import com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.model.despacho.vo.PedidoId;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OrdenDespacho {
    private final EmpresaId empresaId;
    private final DespachoId despachoId;
    private final PedidoId pedidoId;
    private final BodegaId bodegaId;
    private EstadoDespacho estado;
    private final List<LineaDespacho> lineas;
    private final List<Object> domainEvents; // Usamos Object genérico para los eventos

    private OrdenDespacho(EmpresaId empresaId, DespachoId despachoId, PedidoId pedidoId, BodegaId bodegaId, List<LineaDespacho> lineas) {
        this.empresaId = Objects.requireNonNull(empresaId, "El empresaId no puede ser nulo");
        this.despachoId = Objects.requireNonNull(despachoId, "El despachoId no puede ser nulo");
        this.pedidoId = Objects.requireNonNull(pedidoId, "El pedidoId no puede ser nulo");
        this.bodegaId = Objects.requireNonNull(bodegaId, "El bodegaId no puede ser nulo");
        if (lineas == null || lineas.isEmpty()) {
            throw new IllegalArgumentException("La orden de despacho debe tener al menos una línea");
        }
        this.lineas = new ArrayList<>(lineas);
        this.estado = EstadoDespacho.PENDIENTE;
        this.domainEvents = new ArrayList<>();
    }

    public static OrdenDespacho crear(EmpresaId empresaId, DespachoId despachoId, PedidoId pedidoId, BodegaId bodegaId, List<LineaDespacho> lineas) {
        return new OrdenDespacho(empresaId, despachoId, pedidoId, bodegaId, lineas);
    }

    public void iniciarPicking() {
        if (this.estado != EstadoDespacho.PENDIENTE) {
            throw new IllegalStateException("Solo se puede iniciar picking si el despacho está en estado PENDIENTE.");
        }
        this.estado = EstadoDespacho.EN_PICKING;
    }

    public void completarEmpaque() {
        if (this.estado != EstadoDespacho.EN_PICKING) {
            throw new IllegalStateException("Solo se puede empacar si el despacho está EN_PICKING.");
        }
        this.estado = EstadoDespacho.EMPACADO;
    }

    public void confirmarDespacho() {
        if (this.estado != EstadoDespacho.EMPACADO) {
            throw new IllegalStateException("Solo se puede confirmar despacho si el despacho está EMPACADO.");
        }
        this.estado = EstadoDespacho.DESPACHADO;
        this.domainEvents.add(DespachoCompletadoEvent.de(this.empresaId, this.despachoId, this.pedidoId, this.bodegaId));
    }

    public EmpresaId getEmpresaId() { return empresaId; }
    public DespachoId getDespachoId() { return despachoId; }
    public PedidoId getPedidoId() { return pedidoId; }
    public BodegaId getBodegaId() { return bodegaId; }
    public EstadoDespacho getEstado() { return estado; }
    public List<LineaDespacho> getLineas() { return new ArrayList<>(lineas); }
    
    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }
}
