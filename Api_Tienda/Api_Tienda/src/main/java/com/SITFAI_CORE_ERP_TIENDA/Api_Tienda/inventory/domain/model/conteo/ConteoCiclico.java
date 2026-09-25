package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.event.DiscrepanciaInventarioDetectadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.CantidadFisica;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.ConteoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.EstadoConteo;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.ProductoId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Aggregate Root: ConteoCiclico (Cycle Counting / Auditoría Logística WMS).
 * <p>
 * Responsabilidades e Invariantes:
 * <ul>
 *   <li><b>Aislamiento Multi-Tenant (MT-01):</b> Todo conteo encapsula inmutablemente {@link EmpresaId}.</li>
 *   <li><b>Identidad de Dominio (REGLA-3):</b> Encapsula {@link ConteoId} como Value Object.</li>
 *   <li><b>Estado Inicial:</b> Se instancia siempre en estado {@link EstadoConteo#PLANIFICADO}.</li>
 *   <li><b>Consistencia Interna:</b> Debe contener al menos una línea de {@link DetalleConteo}, sin productos duplicados.</li>
 *   <li><b>Transición Activa:</b> {@link #registrarConteoFisico(ProductoId, CantidadFisica)} transiciona a {@link EstadoConteo#EN_EJECUCION}.</li>
 *   <li><b>Cierre y Discrepancias:</b> {@link #finalizar()} valida que todas las líneas hayan sido contadas.
 *       Si todas coinciden, transiciona a {@link EstadoConteo#COMPLETADO}.
 *       Si al menos una difiere, transiciona a {@link EstadoConteo#CON_DISCREPANCIAS} y emite {@link DiscrepanciaInventarioDetectadaEvent}.</li>
 * </ul>
 * <p>
 * Regla REGLA-1: Cero dependencias de JPA, Spring o frameworks en la Capa de Dominio.
 */
public class ConteoCiclico {

    private final ConteoId id;
    private final EmpresaId empresaId;
    private final BodegaId bodegaId;
    private final LocalDate fechaProgramada;
    private EstadoConteo estado;
    private final List<DetalleConteo> detalles;
    private final List<DiscrepanciaInventarioDetectadaEvent> domainEvents;
    private final Instant creadoEn;
    private Instant actualizadoEn;

    private ConteoCiclico(
            ConteoId id,
            EmpresaId empresaId,
            BodegaId bodegaId,
            LocalDate fechaProgramada,
            EstadoConteo estado,
            List<DetalleConteo> detalles,
            Instant creadoEn,
            Instant actualizadoEn) {

        this.id = Objects.requireNonNull(id, "ConteoCiclico: id es obligatorio.");
        this.empresaId = Objects.requireNonNull(empresaId, "ConteoCiclico: empresaId es obligatorio (MT-01).");
        this.bodegaId = Objects.requireNonNull(bodegaId, "ConteoCiclico: bodegaId es obligatorio.");
        this.fechaProgramada = Objects.requireNonNull(fechaProgramada, "ConteoCiclico: fechaProgramada es obligatoria.");
        this.estado = Objects.requireNonNull(estado, "ConteoCiclico: estado es obligatorio.");
        Objects.requireNonNull(detalles, "ConteoCiclico: detalles no puede ser nulo.");
        if (detalles.isEmpty()) {
            throw new IllegalArgumentException("ConteoCiclico: debe incluir al menos una línea de DetalleConteo.");
        }
        validarSinProductosDuplicados(detalles);

        this.detalles = new ArrayList<>(detalles);
        this.domainEvents = new ArrayList<>();
        this.creadoEn = creadoEn != null ? creadoEn : Instant.now();
        this.actualizadoEn = actualizadoEn != null ? actualizadoEn : this.creadoEn;
    }

    // =========================================================================
    // FACTORY METHODS
    // =========================================================================

    /**
     * Factory Method: Crea un nuevo ciclo de conteo en estado PLANIFICADO.
     */
    public static ConteoCiclico iniciar(
            EmpresaId empresaId,
            BodegaId bodegaId,
            LocalDate fechaProgramada,
            List<DetalleConteo> detalles) {
        return new ConteoCiclico(
                ConteoId.generar(),
                empresaId,
                bodegaId,
                fechaProgramada,
                EstadoConteo.PLANIFICADO,
                detalles,
                Instant.now(),
                Instant.now()
        );
    }

    public static ConteoCiclico crear(
            EmpresaId empresaId,
            BodegaId bodegaId,
            LocalDate fechaProgramada,
            List<DetalleConteo> detalles) {
        return iniciar(empresaId, bodegaId, fechaProgramada, detalles);
    }

    public static ConteoCiclico crear(
            ConteoId id,
            EmpresaId empresaId,
            BodegaId bodegaId,
            LocalDate fechaProgramada,
            List<DetalleConteo> detalles) {
        return new ConteoCiclico(
                id,
                empresaId,
                bodegaId,
                fechaProgramada,
                EstadoConteo.PLANIFICADO,
                detalles,
                Instant.now(),
                Instant.now()
        );
    }

    /**
     * Factory Method de reconstitución para adaptadores de persistencia (sin generar eventos).
     */
    public static ConteoCiclico reconstituir(
            ConteoId id,
            EmpresaId empresaId,
            BodegaId bodegaId,
            LocalDate fechaProgramada,
            EstadoConteo estado,
            List<DetalleConteo> detalles,
            Instant creadoEn,
            Instant actualizadoEn) {
        return new ConteoCiclico(
                id,
                empresaId,
                bodegaId,
                fechaProgramada,
                estado,
                detalles,
                creadoEn,
                actualizadoEn
        );
    }

    // =========================================================================
    // COMPORTAMIENTO Y REGLAS DE NEGOCIO
    // =========================================================================

    /**
     * Registra o actualiza la cantidad física constatada para un producto.
     * Transiciona el estado a {@link EstadoConteo#EN_EJECUCION}.
     *
     * @param productoId Producto auditado.
     * @param cantidadFisica Cantidad real contada en estantería/bodega.
     */
    public void registrarConteoFisico(ProductoId productoId, CantidadFisica cantidadFisica) {
        Objects.requireNonNull(productoId, "ConteoCiclico: productoId es obligatorio.");
        Objects.requireNonNull(cantidadFisica, "ConteoCiclico: cantidadFisica es obligatoria.");

        if (this.estado.esFinalizado()) {
            throw new IllegalStateException(
                    "Operación inválida: no se puede registrar conteo físico en un ciclo finalizado con estado " + this.estado + ".");
        }

        DetalleConteo detalle = buscarDetallePorProducto(productoId);
        detalle.registrarConteoFisico(cantidadFisica);

        this.estado = EstadoConteo.EN_EJECUCION;
        this.actualizadoEn = Instant.now();
    }

    /**
     * Finaliza la auditoría del conteo cíclico evaluando todas las líneas.
     * <ul>
     *   <li>Si todas las cantidades físicas coinciden con las teóricas: estado pasa a {@link EstadoConteo#COMPLETADO}.</li>
     *   <li>Si alguna cantidad difiere: estado pasa a {@link EstadoConteo#CON_DISCREPANCIAS} y emite {@link DiscrepanciaInventarioDetectadaEvent}.</li>
     * </ul>
     */
    public void finalizar() {
        if (this.estado.esFinalizado()) {
            throw new IllegalStateException(
                    "Operación inválida: el conteo cíclico ya se encuentra finalizado con estado " + this.estado + ".");
        }

        // Validar que todas las líneas hayan sido efectivamente auditadas
        for (DetalleConteo d : detalles) {
            if (!d.fueContado()) {
                throw new IllegalStateException(
                        "No se puede finalizar el conteo cíclico: el producto " + d.getProductoId() + " no tiene cantidad física registrada.");
            }
        }

        List<DiscrepanciaInventarioDetectadaEvent.DiscrepanciaItem> discrepancias = new ArrayList<>();
        for (DetalleConteo d : detalles) {
            if (d.tieneDiscrepancia()) {
                discrepancias.add(DiscrepanciaInventarioDetectadaEvent.DiscrepanciaItem.de(
                        d.getProductoId(),
                        d.getCantidadTeorica(),
                        d.getCantidadFisica().valor()
                ));
            }
        }

        if (discrepancias.isEmpty()) {
            this.estado = EstadoConteo.COMPLETADO;
        } else {
            this.estado = EstadoConteo.CON_DISCREPANCIAS;
            // Registrar evento inmutable de dominio
            this.domainEvents.add(DiscrepanciaInventarioDetectadaEvent.of(
                    this.empresaId,
                    this.id,
                    this.bodegaId,
                    discrepancias
            ));
        }

        this.actualizadoEn = Instant.now();
    }

    private DetalleConteo buscarDetallePorProducto(ProductoId productoId) {
        return this.detalles.stream()
                .filter(d -> Objects.equals(d.getProductoId(), productoId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "El producto " + productoId + " no forma parte de este conteo cíclico."));
    }

    private static void validarSinProductosDuplicados(List<DetalleConteo> detalles) {
        Set<ProductoId> productos = new HashSet<>();
        for (DetalleConteo d : detalles) {
            if (!productos.add(d.getProductoId())) {
                throw new IllegalArgumentException(
                        "ConteoCiclico: lista de detalles contiene productos duplicados: " + d.getProductoId());
            }
        }
    }

    // =========================================================================
    // GESTIÓN DE EVENTOS DE DOMINIO (REGLA-3)
    // =========================================================================

    public List<DiscrepanciaInventarioDetectadaEvent> pullDomainEvents() {
        List<DiscrepanciaInventarioDetectadaEvent> eventos = Collections.unmodifiableList(new ArrayList<>(this.domainEvents));
        this.domainEvents.clear();
        return eventos;
    }

    public List<DiscrepanciaInventarioDetectadaEvent> drainDomainEvents() {
        return pullDomainEvents();
    }

    public List<DiscrepanciaInventarioDetectadaEvent> peekDomainEvents() {
        return Collections.unmodifiableList(this.domainEvents);
    }

    // =========================================================================
    // GETTERS (SOLO LECTURA)
    // =========================================================================

    public ConteoId getId() {
        return id;
    }

    public EmpresaId getEmpresaId() {
        return empresaId;
    }

    public BodegaId getBodegaId() {
        return bodegaId;
    }

    public LocalDate getFechaProgramada() {
        return fechaProgramada;
    }

    public EstadoConteo getEstado() {
        return estado;
    }

    public List<DetalleConteo> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConteoCiclico that)) return false;
        return Objects.equals(id, that.id) && Objects.equals(empresaId, that.empresaId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, empresaId);
    }

    @Override
    public String toString() {
        return "ConteoCiclico{" +
                "id=" + id +
                ", empresaId=" + empresaId +
                ", bodegaId=" + bodegaId +
                ", estado=" + estado +
                ", lineas=" + detalles.size() +
                '}';
    }
}
