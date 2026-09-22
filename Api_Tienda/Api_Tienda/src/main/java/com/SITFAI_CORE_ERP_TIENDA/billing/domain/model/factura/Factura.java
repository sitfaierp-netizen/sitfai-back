package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura;

import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.FacturaAnuladaEvent;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.event.FacturaEmitidaEvent;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception.FacturaInvalidaException;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.EstadoFactura;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo.FacturaId;
import com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject.Dinero;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root: {@code Factura} en el Bounded Context de Facturación (Puerto 8085).
 * <p>
 * Responsabilidades e Invariantes:
 * <ul>
 *   <li><b>Aislamiento Multitenant (MT-01):</b> Pertenece estrictamente a una sola {@link EmpresaId}.</li>
 *   <li><b>Trazabilidad Documental (AUD-04):</b> Enlaza directamente a un {@link DocumentoFuenteId} (POS / E-commerce).</li>
 *   <li><b>Inmutabilidad Financiera (DOC-01, AUD-04):</b> Una vez emitida, una factura no se altera ni elimina; solo se puede anular.</li>
 *   <li><b>Integridad Matemática:</b> No admite emisión con lista vacía de líneas ni montos negativos; calcula automáticamente subtotal, impuestos y total.</li>
 *   <li><b>Event Sourcing & Coreografía:</b> Emite {@link FacturaEmitidaEvent} y {@link FacturaAnuladaEvent}.</li>
 * </ul>
 * <p>
 * Pureza absoluta: Cero dependencias técnicas de JPA, Spring o Jackson (REGLA-1, REGLA-3, MCP-01).
 */
public class Factura {

    private final FacturaId id;
    private final EmpresaId empresaId;
    private final DocumentoFuenteId documentoFuenteId;
    private final ClienteId clienteId;
    private EstadoFactura estado;
    private final List<LineaFactura> lineas;
    private Dinero subtotal;
    private Dinero totalImpuestos;
    private Dinero total;
    private final List<DomainEvent> domainEvents;
    private final Instant emitidoEn;
    private Instant anuladoEn;
    private String motivoAnulacion;

    private Factura(
            FacturaId id,
            EmpresaId empresaId,
            DocumentoFuenteId documentoFuenteId,
            ClienteId clienteId,
            EstadoFactura estado,
            List<LineaFactura> lineas,
            Dinero subtotal,
            Dinero totalImpuestos,
            Dinero total,
            Instant emitidoEn,
            Instant anuladoEn,
            String motivoAnulacion) {

        this.id = Objects.requireNonNull(id, "Factura: id es obligatorio.");
        this.empresaId = Objects.requireNonNull(empresaId, "Factura: empresaId es obligatorio (MT-01).");
        this.documentoFuenteId = Objects.requireNonNull(documentoFuenteId, "Factura: documentoFuenteId es obligatorio.");
        this.clienteId = Objects.requireNonNull(clienteId, "Factura: clienteId es obligatorio.");
        this.estado = Objects.requireNonNull(estado, "Factura: estado es obligatorio.");
        this.lineas = new ArrayList<>(Objects.requireNonNull(lineas, "Factura: lineas no puede ser null."));
        this.subtotal = Objects.requireNonNull(subtotal, "Factura: subtotal es obligatorio.");
        this.totalImpuestos = Objects.requireNonNull(totalImpuestos, "Factura: totalImpuestos es obligatorio.");
        this.total = Objects.requireNonNull(total, "Factura: total es obligatorio.");
        this.domainEvents = new ArrayList<>();
        this.emitidoEn = Objects.requireNonNull(emitidoEn, "Factura: emitidoEn es obligatorio.");
        this.anuladoEn = anuladoEn;
        this.motivoAnulacion = motivoAnulacion;
    }

    /**
     * Factory Method principal para emitir una Factura.
     * <p>
     * Calcula automáticamente el total matemático de las líneas (subtotal + impuestos)
     * y aplica validaciones fail-fast si las líneas están vacías o contienen montos inválidos.
     * Transiciona el estado a {@link EstadoFactura#EMITIDA} y registra {@link FacturaEmitidaEvent}.
     *
     * @param id                Identificador único de la Factura.
     * @param empresaId         Identificador del Tenant raíz (MT-01).
     * @param documentoFuenteId Enlace al documento origen (POS o E-commerce).
     * @param clienteId         Identificador del Cliente receptor.
     * @param lineas            Detalle de líneas a facturar.
     * @return Instancia del Agregado {@code Factura} en estado {@code EMITIDA}.
     * @throws FacturaInvalidaException si no cumple con las invariantes de negocio.
     */
    public static Factura emitir(
            FacturaId id,
            EmpresaId empresaId,
            DocumentoFuenteId documentoFuenteId,
            ClienteId clienteId,
            List<LineaFactura> lineas) {

        if (id == null) {
            throw new FacturaInvalidaException("Factura: id es obligatorio.");
        }
        if (empresaId == null) {
            throw new FacturaInvalidaException("Factura: empresaId es obligatorio (MT-01).");
        }
        if (documentoFuenteId == null) {
            throw new FacturaInvalidaException("Factura: documentoFuenteId es obligatorio.");
        }
        if (clienteId == null) {
            throw new FacturaInvalidaException("Factura: clienteId es obligatorio.");
        }
        if (lineas == null || lineas.isEmpty()) {
            throw new FacturaInvalidaException("Una factura no puede emitirse sin líneas de detalle.");
        }

        // Validación fail-fast y cálculo matemático exacto de totales
        Dinero subtotalAcumulado = Dinero.cero();
        Dinero impuestosAcumulados = Dinero.cero();

        for (LineaFactura linea : lineas) {
            if (linea == null) {
                throw new FacturaInvalidaException("Las líneas de la factura no pueden ser nulas.");
            }
            if (linea.getCantidad() == null || linea.getCantidad().compareTo(BigDecimal.ZERO) <= 0) {
                throw new FacturaInvalidaException("La cantidad en cada línea debe ser mayor a cero.");
            }
            if (linea.getPrecioUnitario() == null || linea.getPrecioUnitario().monto().compareTo(BigDecimal.ZERO) < 0) {
                throw new FacturaInvalidaException("El precio unitario no puede ser negativo.");
            }
            subtotalAcumulado = subtotalAcumulado.sumar(linea.calcularSubtotal());
            impuestosAcumulados = impuestosAcumulados.sumar(linea.calcularTotalImpuestos());
        }

        Dinero totalGeneral = subtotalAcumulado.sumar(impuestosAcumulados);
        if (totalGeneral.monto().compareTo(BigDecimal.ZERO) < 0) {
            throw new FacturaInvalidaException("El total de la factura no puede ser negativo.");
        }

        Instant ahora = Instant.now();

        Factura factura = new Factura(
                id,
                empresaId,
                documentoFuenteId,
                clienteId,
                EstadoFactura.EMITIDA,
                lineas,
                subtotalAcumulado,
                impuestosAcumulados,
                totalGeneral,
                ahora,
                null,
                null
        );

        // Registro de Domain Event
        factura.domainEvents.add(FacturaEmitidaEvent.of(
                id,
                empresaId,
                documentoFuenteId,
                clienteId,
                totalGeneral
        ));

        return factura;
    }

    /**
     * Reconstituye el Agregado desde persistencia / repositorio.
     */
    public static Factura reconstituir(
            FacturaId id,
            EmpresaId empresaId,
            DocumentoFuenteId documentoFuenteId,
            ClienteId clienteId,
            EstadoFactura estado,
            List<LineaFactura> lineas,
            Dinero subtotal,
            Dinero totalImpuestos,
            Dinero total,
            Instant emitidoEn,
            Instant anuladoEn,
            String motivoAnulacion) {

        return new Factura(
                id, empresaId, documentoFuenteId, clienteId, estado, lineas,
                subtotal, totalImpuestos, total, emitidoEn, anuladoEn, motivoAnulacion
        );
    }

    /**
     * Método transaccional de dominio: Anula la factura por un motivo específico (AUD-04).
     * <p>
     * Valida fail-fast que la factura se encuentre en estado {@link EstadoFactura#EMITIDA}
     * y que el motivo no sea vacío. Transiciona el estado a {@link EstadoFactura#ANULADA}
     * y emite {@link FacturaAnuladaEvent}.
     *
     * @param motivo Explicación fiscal o comercial de la anulación.
     * @throws FacturaInvalidaException si la factura ya está anulada o el motivo es inválido.
     */
    public void anular(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            throw new FacturaInvalidaException("El motivo de anulación es obligatorio (AUD-04).");
        }

        if (this.estado != EstadoFactura.EMITIDA) {
            throw new FacturaInvalidaException(
                    String.format("No se puede anular la factura: solo se pueden anular facturas en estado EMITIDA. Estado actual: '%s'.", this.estado)
            );
        }

        this.estado = EstadoFactura.ANULADA;
        this.motivoAnulacion = motivo.trim();
        this.anuladoEn = Instant.now();

        // Emisión de FacturaAnuladaEvent
        this.domainEvents.add(FacturaAnuladaEvent.of(
                this.id,
                this.empresaId,
                this.motivoAnulacion
        ));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GESTIÓN DE DOMAIN EVENTS (AUD-03)
    // ═════════════════════════════════════════════════════════════════════════

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(this.domainEvents);
    }

    public List<DomainEvent> drainDomainEvents() {
        List<DomainEvent> eventos = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(eventos);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GETTERS
    // ═════════════════════════════════════════════════════════════════════════

    public FacturaId getId() {
        return id;
    }

    public EmpresaId getEmpresaId() {
        return empresaId;
    }

    public DocumentoFuenteId getDocumentoFuenteId() {
        return documentoFuenteId;
    }

    public ClienteId getClienteId() {
        return clienteId;
    }

    public EstadoFactura getEstado() {
        return estado;
    }

    public List<LineaFactura> getLineas() {
        return Collections.unmodifiableList(lineas);
    }

    public Dinero getSubtotal() {
        return subtotal;
    }

    public Dinero getTotalImpuestos() {
        return totalImpuestos;
    }

    public Dinero getTotal() {
        return total;
    }

    public Instant getEmitidoEn() {
        return emitidoEn;
    }

    public Instant getAnuladoEn() {
        return anuladoEn;
    }

    public String getMotivoAnulacion() {
        return motivoAnulacion;
    }
}
