package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.DespachoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.DespachoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.EstadoDespacho;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.despacho.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root: Despacho de Inventario (Logística de Salida / Outbound Logistics).
 * <p>
 * Regla 1 (Clean Architecture): POJO puro sin anotaciones de persistencia o Spring.
 * Regla 3 (DDD): Encapsulamiento total de invariantes y emisión de Domain Events.
 * Regla MT-01: Aislamiento estricto por {@code empresaId}.
 * Regla BOD-04: Referencia obligatoria a {@code pedidoId} como documento fuente.
 */
public class Despacho {

    private final DespachoId id;
    private final EmpresaId empresaId;
    private final PedidoId pedidoId;
    private final BodegaId bodegaId;
    private EstadoDespacho estado;
    private final List<LineaDespacho> lineas;
    private final List<DomainEvent> domainEvents;
    private final Instant createdAt;
    private Instant updatedAt;

    private Despacho(
            DespachoId id,
            EmpresaId empresaId,
            PedidoId pedidoId,
            BodegaId bodegaId,
            EstadoDespacho estado,
            List<LineaDespacho> lineas,
            Instant createdAt,
            Instant updatedAt) {

        this.id = Objects.requireNonNull(id, "Despacho: id es obligatorio.");
        this.empresaId = Objects.requireNonNull(empresaId, "Despacho: empresaId es obligatorio (MT-01).");
        this.pedidoId = Objects.requireNonNull(pedidoId, "Despacho: pedidoId es obligatorio (BOD-04).");
        this.bodegaId = bodegaId;
        this.estado = Objects.requireNonNull(estado, "Despacho: estado es obligatorio.");
        this.lineas = lineas != null ? new ArrayList<>(lineas) : new ArrayList<>();
        this.domainEvents = new ArrayList<>();
        this.createdAt = Objects.requireNonNull(createdAt, "Despacho: createdAt es obligatorio.");
        this.updatedAt = Objects.requireNonNull(updatedAt, "Despacho: updatedAt es obligatorio.");
    }

    /**
     * Factory method para instanciar un nuevo Despacho en estado PENDIENTE.
     */
    public static Despacho crear(DespachoId id, EmpresaId empresaId, PedidoId pedidoId) {
        return crear(id, empresaId, pedidoId, null, new ArrayList<>());
    }

    /**
     * Factory method para instanciar un nuevo Despacho con líneas iniciales en estado PENDIENTE.
     */
    public static Despacho crear(DespachoId id, EmpresaId empresaId, PedidoId pedidoId, List<LineaDespacho> lineas) {
        return crear(id, empresaId, pedidoId, null, lineas);
    }

    /**
     * Factory method para instanciar un nuevo Despacho con bodega de origen y líneas iniciales.
     */
    public static Despacho crear(DespachoId id, EmpresaId empresaId, PedidoId pedidoId, BodegaId bodegaId, List<LineaDespacho> lineas) {
        Instant ahora = Instant.now();
        return new Despacho(id, empresaId, pedidoId, bodegaId, EstadoDespacho.PENDIENTE, lineas, ahora, ahora);
    }

    /**
     * Factory method para instanciar un nuevo Despacho con bodega de origen.
     */
    public static Despacho crear(DespachoId id, EmpresaId empresaId, PedidoId pedidoId, BodegaId bodegaId) {
        return crear(id, empresaId, pedidoId, bodegaId, new ArrayList<>());
    }

    /**
     * Reconstituye el agregado desde la capa de persistencia (Infraestructura).
     */
    public static Despacho reconstituir(
            DespachoId id,
            EmpresaId empresaId,
            PedidoId pedidoId,
            BodegaId bodegaId,
            EstadoDespacho estado,
            List<LineaDespacho> lineas,
            Instant createdAt,
            Instant updatedAt) {
        return new Despacho(id, empresaId, pedidoId, bodegaId, estado, lineas, createdAt, updatedAt);
    }

    /**
     * Agrega una línea al despacho. Solo permitido si se encuentra en estado PENDIENTE.
     */
    public void agregarLinea(LineaDespacho linea) {
        if (this.estado != EstadoDespacho.PENDIENTE) {
            throw new IllegalStateException("No se pueden agregar líneas a un despacho en estado: " + this.estado);
        }
        Objects.requireNonNull(linea, "La línea de despacho no puede ser nula.");
        this.lineas.add(linea);
        this.updatedAt = Instant.now();
    }

    /**
     * Transiciona el estado a EN_PICKING (preparación física en almacén).
     */
    public void iniciarPicking() {
        if (this.estado != EstadoDespacho.PENDIENTE) {
            throw new IllegalStateException("Solo se puede iniciar picking desde estado PENDIENTE. Estado actual: " + this.estado);
        }
        if (this.lineas.isEmpty()) {
            throw new IllegalStateException("No se puede iniciar picking de un despacho sin líneas asignadas.");
        }
        this.estado = EstadoDespacho.EN_PICKING;
        this.updatedAt = Instant.now();
    }

    /**
     * Transiciona el estado a EMPACADO (listo para salida en andén).
     */
    public void empacar() {
        if (this.estado != EstadoDespacho.EN_PICKING) {
            throw new IllegalStateException("Solo se puede empacar un despacho en estado EN_PICKING. Estado actual: " + this.estado);
        }
        this.estado = EstadoDespacho.EMPACADO;
        this.updatedAt = Instant.now();
    }

    /**
     * Método transaccional de dominio: Confirma la salida física de mercancía.
     * <p>
     * Valida (fail-fast) que haya líneas asignadas, transiciona el estado a DESPACHADO
     * y emite el evento de dominio {@link DespachoConfirmadoEvent}.
     */
    public void confirmarDespacho() {
        if (this.lineas.isEmpty()) {
            throw new IllegalStateException("No se puede confirmar un despacho sin líneas asignadas.");
        }
        if (this.estado == EstadoDespacho.DESPACHADO) {
            throw new IllegalStateException("El despacho ya ha sido confirmado y despachado.");
        }

        this.estado = EstadoDespacho.DESPACHADO;
        this.updatedAt = Instant.now();

        List<DespachoConfirmadoEvent.LineaDespachoDetalle> detalles = this.lineas.stream()
                .map(l -> new DespachoConfirmadoEvent.LineaDespachoDetalle(l.getProductoId(), l.getCantidad()))
                .toList();

        this.domainEvents.add(DespachoConfirmadoEvent.of(
                this.empresaId,
                this.bodegaId,
                this.id,
                this.pedidoId,
                detalles
        ));
    }

    /**
     * Drena los eventos de dominio acumulados (Pull Pattern para la capa de Aplicación).
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> eventos = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(eventos);
    }

    // --- Accessors ---

    public DespachoId getId() {
        return id;
    }

    public EmpresaId getEmpresaId() {
        return empresaId;
    }

    public PedidoId getPedidoId() {
        return pedidoId;
    }

    public BodegaId getBodegaId() {
        return bodegaId;
    }

    public EstadoDespacho getEstado() {
        return estado;
    }

    public List<LineaDespacho> getLineas() {
        return Collections.unmodifiableList(lineas);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
