package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.DocumentoTransaccional;
import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.enums.DocumentStatus;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.TicketEmitidoEvent;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.vo.TicketId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TurnoId;
import com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception.DocumentStateException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * Aggregate Root: Ticket de Venta POS.
 *
 * Implementa el contrato {@link DocumentoTransaccional}:
 * - Lleva empresa_id como clave de partición (MT-01).
 * - Su estado es inmutable una vez PAGADO/ANULADO (DOC-01).
 * - Emite {@link TicketEmitidoEvent} al confirmarse el pago, desencadenando la deducción FEFO en inventario.
 */
public class TicketVenta implements DocumentoTransaccional {

    private final TicketId id;
    private final UUID empresaId;       // MT-01: obligatorio
    private final CajaId cajaId;
    private final TurnoId turnoId;
    private final String cajero;        // AUD-01

    private final List<LineaTicket> lineas;
    private MetodoPago metodoPago;
    private DocumentStatus estado;
    private Dinero totalGeneral;

    private final Instant creadoEn;
    private Instant actualizadoEn;

    private final List<DomainEvent> domainEvents;

    // Constructor privado: solo se construye vía factory methods
    private TicketVenta(TicketId id, UUID empresaId, CajaId cajaId, TurnoId turnoId, String cajero) {
        this.id = Objects.requireNonNull(id, "TicketId es obligatorio");
        this.empresaId = Objects.requireNonNull(empresaId, "EmpresaId es obligatorio (MT-01)");
        this.cajaId = Objects.requireNonNull(cajaId, "CajaId es obligatorio");
        this.turnoId = Objects.requireNonNull(turnoId, "TurnoId es obligatorio");
        this.cajero = Objects.requireNonNull(cajero, "El cajero es obligatorio (AUD-01)");
        this.estado = DocumentStatus.BORRADOR;
        this.lineas = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
        this.totalGeneral = Dinero.cero();
        this.creadoEn = Instant.now();
        this.actualizadoEn = this.creadoEn;
    }

    /**
     * Factory method: abre un nuevo ticket en estado BORRADOR.
     */
    public static TicketVenta abrir(UUID empresaId, CajaId cajaId, TurnoId turnoId, String cajero) {
        return new TicketVenta(TicketId.generar(), empresaId, cajaId, turnoId, cajero);
    }

    /**
     * Factory method de reconstitución desde persistencia.
     */
    public static TicketVenta reconstituir(TicketId id, UUID empresaId, CajaId cajaId, TurnoId turnoId,
                                           String cajero, List<LineaTicket> lineas,
                                           MetodoPago metodoPago, DocumentStatus estado,
                                           Dinero totalGeneral, Instant creadoEn) {
        TicketVenta ticket = new TicketVenta(id, empresaId, cajaId, turnoId, cajero);
        ticket.lineas.addAll(lineas != null ? lineas : Collections.emptyList());
        ticket.metodoPago = metodoPago;
        ticket.estado = estado;
        ticket.totalGeneral = totalGeneral;
        return ticket;
    }

    /**
     * Agrega un ítem al ticket. Solo permitido en BORRADOR.
     * @throws DocumentStateException si el ticket no está en BORRADOR.
     */
    public void agregarLinea(ProductoId productoId, String descripcion, BigDecimal cantidad, Dinero precioUnitario) {
        if (!puedeEditar()) {
            throw new DocumentStateException("No se pueden agregar líneas a un ticket que no está en BORRADOR.");
        }
        Objects.requireNonNull(productoId, "ProductoId es obligatorio");
        Objects.requireNonNull(descripcion, "La descripción es obligatoria");

        LineaTicket linea = new LineaTicket(productoId, descripcion, cantidad, precioUnitario);
        this.lineas.add(linea);
        recalcularTotal();
        this.actualizadoEn = Instant.now();
    }

    /**
     * Registra el pago y pasa el ticket a EMITIDO.
     * Emite TicketEmitidoEvent para desencadenar deducción FEFO en inventario.
     * CAJ-01: El ticket debe tener al menos una línea para ser pagado.
     */
    public void pagar(MetodoPago metodoPago) {
        if (!puedeEditar()) {
            throw new DocumentStateException("Solo se puede pagar un ticket en estado BORRADOR.");
        }
        if (this.lineas.isEmpty()) {
            throw new IllegalStateException("No se puede pagar un ticket sin líneas (CAJ-01).");
        }
        Objects.requireNonNull(metodoPago, "El método de pago es obligatorio");

        this.metodoPago = metodoPago;
        cambiarEstado(DocumentStatus.EMITIDO);
        this.actualizadoEn = Instant.now();

        // Emitir evento crítico → desencadena deducción FEFO en inventario
        this.domainEvents.add(new TicketEmitidoEvent(this.id, this.empresaId, this.cajaId, this.turnoId,
                Collections.unmodifiableList(this.lineas), this.totalGeneral, this.metodoPago));
    }

    /**
     * Anula el ticket. Solo se puede anular si ya fue EMITIDO (DOC-01).
     */
    @Override
    public void anular() {
        DocumentoTransaccional.super.anular();
        this.actualizadoEn = Instant.now();
    }

    /**
     * Extrae y limpia los eventos de dominio pendientes de publicación.
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> eventos = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(eventos);
    }

    // ── Contrato DocumentoTransaccional ──────────────────────────────────────

    @Override
    public DocumentStatus getEstado() { return estado; }

    @Override
    public Instant getCreatedAt() { return creadoEn; }

    @Override
    public String getCreatedBy() { return cajero; }

    @Override
    public void cambiarEstado(DocumentStatus nuevoEstado) {
        this.estado = Objects.requireNonNull(nuevoEstado);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public TicketId getId() { return id; }
    public UUID getEmpresaId() { return empresaId; }
    public CajaId getCajaId() { return cajaId; }
    public TurnoId getTurnoId() { return turnoId; }
    public String getCajero() { return cajero; }
    public List<LineaTicket> getLineas() { return Collections.unmodifiableList(lineas); }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public Dinero getTotalGeneral() { return totalGeneral; }
    public Instant getActualizadoEn() { return actualizadoEn; }

    // ── Lógica interna ───────────────────────────────────────────────────────

    private void recalcularTotal() {
        this.totalGeneral = lineas.stream()
                .map(LineaTicket::calcularSubtotal)
                .reduce(Dinero.cero(), Dinero::sumar);
    }
}
