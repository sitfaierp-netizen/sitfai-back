package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.event.OrdenCompraEmitidaEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.EstadoOrdenCompra;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model.ordencompra.vo.ProveedorId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root: {@code OrdenCompra} en el Bounded Context de Compras / Abastecimiento (BOD-04).
 * <p>
 * Responsabilidades e Invariantes:
 * <ul>
 *   <li><b>Aislamiento Multitenant (MT-01):</b> Pertenece estrictamente a una sola {@link EmpresaId}.</li>
 *   <li><b>Trazabilidad Documental (BOD-04):</b> Actúa como documento fuente primario para el ingreso de stock en bodega.</li>
 *   <li><b>Máquina de Estados Estricta (PUR-01):</b> Transiciona de {@code BORRADOR} a {@code EMITIDA}, {@code RECEPCION_PARCIAL}, {@code COMPLETADA} o {@code CANCELADA}.</li>
 *   <li><b>Invariante de Emisión:</b> Prohibido emitir una orden sin al menos una línea de detalle.</li>
 * </ul>
 * <p>
 * Pureza absoluta: Cero dependencias técnicas de JPA, Spring o Jackson (REGLA-1, REGLA-3, MCP-01).
 */
public class OrdenCompra {

    private final OrdenCompraId id;
    private final EmpresaId empresaId;
    private final ProveedorId proveedorId;

    private EstadoOrdenCompra estado;
    private final List<LineaOrdenCompra> lineas;
    private final List<DomainEvent> domainEvents;

    private final Instant creadoEn;
    private Instant emitidoEn;

    private OrdenCompra(
            OrdenCompraId id,
            EmpresaId empresaId,
            ProveedorId proveedorId,
            EstadoOrdenCompra estado,
            List<LineaOrdenCompra> lineas,
            Instant creadoEn,
            Instant emitidoEn
    ) {
        this.id = Objects.requireNonNull(id, "OrdenCompra: id no puede ser nulo.");
        this.empresaId = Objects.requireNonNull(empresaId, "OrdenCompra: empresaId no puede ser nulo (MT-01).");
        this.proveedorId = Objects.requireNonNull(proveedorId, "OrdenCompra: proveedorId no puede ser nulo.");
        this.estado = Objects.requireNonNull(estado, "OrdenCompra: estado no puede ser nulo.");
        this.lineas = lineas != null ? new ArrayList<>(lineas) : new ArrayList<>();
        this.domainEvents = new ArrayList<>();
        this.creadoEn = creadoEn != null ? creadoEn : Instant.now();
        this.emitidoEn = emitidoEn;
    }

    /**
     * Factory Method principal: Inicializa una nueva Orden de Compra en estado {@link EstadoOrdenCompra#BORRADOR}.
     */
    public static OrdenCompra crear(OrdenCompraId id, EmpresaId empresaId, ProveedorId proveedorId) {
        return new OrdenCompra(
                id,
                empresaId,
                proveedorId,
                EstadoOrdenCompra.BORRADOR,
                new ArrayList<>(),
                Instant.now(),
                null
        );
    }

    /**
     * Factory Method conveniente: Inicializa una orden autogenerando su identificador.
     */
    public static OrdenCompra crear(EmpresaId empresaId, ProveedorId proveedorId) {
        return crear(OrdenCompraId.generar(), empresaId, proveedorId);
    }

    /**
     * Reconstituye el agregado desde la capa de persistencia/infraestructura.
     */
    public static OrdenCompra reconstituir(
            OrdenCompraId id,
            EmpresaId empresaId,
            ProveedorId proveedorId,
            EstadoOrdenCompra estado,
            List<LineaOrdenCompra> lineas,
            Instant creadoEn,
            Instant emitidoEn
    ) {
        return new OrdenCompra(id, empresaId, proveedorId, estado, lineas, creadoEn, emitidoEn);
    }

    /**
     * Agrega una nueva línea de producto a la orden de compra.
     * Solo permitido si la orden se encuentra en estado {@link EstadoOrdenCompra#BORRADOR}.
     */
    public void agregarLinea(ProductoId productoId, int cantidadSolicitada, BigDecimal costoUnitarioEsperado) {
        if (!this.estado.puedeModificarse()) {
            throw new IllegalStateException("OrdenCompra: No se pueden agregar líneas a una orden en estado " + this.estado);
        }
        LineaOrdenCompra nuevaLinea = LineaOrdenCompra.crear(productoId, cantidadSolicitada, costoUnitarioEsperado);
        this.lineas.add(nuevaLinea);
    }

    /**
     * Agrega una entidad {@link LineaOrdenCompra} ya instanciada.
     */
    public void agregarLinea(LineaOrdenCompra linea) {
        Objects.requireNonNull(linea, "OrdenCompra: la línea a agregar no puede ser nula.");
        if (!this.estado.puedeModificarse()) {
            throw new IllegalStateException("OrdenCompra: No se pueden agregar líneas a una orden en estado " + this.estado);
        }
        this.lineas.add(linea);
    }

    /**
     * Método transaccional de dominio: Emite la orden al proveedor adjudicado.
     * <p>
     * Invariantes validadas (fail-fast):
     * <ul>
     *   <li>La orden debe estar en estado {@code BORRADOR}.</li>
     *   <li>La orden debe tener al menos una línea de detalle.</li>
     * </ul>
     * Transiciona el estado a {@code EMITIDA}, sella la fecha de emisión y registra {@link OrdenCompraEmitidaEvent}.
     */
    public void emitirAlProveedor() {
        if (!this.estado.puedeEmitirse()) {
            throw new IllegalStateException("OrdenCompra: Solo se puede emitir una orden que esté en estado BORRADOR. Estado actual: " + this.estado);
        }
        if (this.lineas.isEmpty()) {
            throw new IllegalStateException("OrdenCompra: No se puede emitir una Orden de Compra sin líneas de detalle (BOD-04).");
        }

        this.estado = EstadoOrdenCompra.EMITIDA;
        this.emitidoEn = Instant.now();

        BigDecimal totalEsperado = calcularTotalEsperado();
        OrdenCompraEmitidaEvent evento = OrdenCompraEmitidaEvent.crear(
                this.empresaId,
                this.id,
                this.proveedorId,
                this.lineas.size(),
                totalEsperado
        );
        this.domainEvents.add(evento);
    }

    /**
     * Transiciona el estado a {@link EstadoOrdenCompra#RECEPCION_PARCIAL} al recibirse mercancía parcial en almacén.
     */
    public void marcarRecepcionParcial() {
        if (this.estado != EstadoOrdenCompra.EMITIDA && this.estado != EstadoOrdenCompra.RECEPCION_PARCIAL) {
            throw new IllegalStateException("OrdenCompra: No se puede registrar recepción parcial sobre una orden en estado " + this.estado);
        }
        this.estado = EstadoOrdenCompra.RECEPCION_PARCIAL;
    }

    /**
     * Transiciona el estado a {@link EstadoOrdenCompra#COMPLETADA} al completarse el ingreso total.
     */
    public void marcarCompletada() {
        if (this.estado != EstadoOrdenCompra.EMITIDA && this.estado != EstadoOrdenCompra.RECEPCION_PARCIAL) {
            throw new IllegalStateException("OrdenCompra: No se puede completar una orden en estado " + this.estado);
        }
        this.estado = EstadoOrdenCompra.COMPLETADA;
    }

    /**
     * Cancela la orden de compra.
     */
    public void cancelar(String motivo) {
        if (this.estado == EstadoOrdenCompra.COMPLETADA) {
            throw new IllegalStateException("OrdenCompra: Prohibido cancelar una Orden de Compra que ya fue COMPLETADA.");
        }
        if (this.estado == EstadoOrdenCompra.CANCELADA) {
            return;
        }
        this.estado = EstadoOrdenCompra.CANCELADA;
    }

    /**
     * Suma los subtotales esperados de todas las líneas de detalle.
     */
    public BigDecimal calcularTotalEsperado() {
        return this.lineas.stream()
                .map(LineaOrdenCompra::getSubtotalEsperado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // --- Getters y Encapsulamiento ---

    public OrdenCompraId getId() {
        return id;
    }

    public EmpresaId getEmpresaId() {
        return empresaId;
    }

    public ProveedorId getProveedorId() {
        return proveedorId;
    }

    public EstadoOrdenCompra getEstado() {
        return estado;
    }

    public List<LineaOrdenCompra> getLineas() {
        return Collections.unmodifiableList(lineas);
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getEmitidoEn() {
        return emitidoEn;
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void limpiarEventos() {
        this.domainEvents.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrdenCompra that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "OrdenCompra{" +
                "id=" + id +
                ", empresaId=" + empresaId +
                ", proveedorId=" + proveedorId +
                ", estado=" + estado +
                ", totalLineas=" + lineas.size() +
                ", creadoEn=" + creadoEn +
                ", emitidoEn=" + emitidoEn +
                '}';
    }
}
