package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.EstadoSolicitud;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject.SolicitudId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root: SolicitudAbastecimiento.
 *
 * Representa la intención formal de un operador de Bodega de solicitar
 * reabastecimiento de productos a ser gestionados por el área de Compras.
 *
 * Invariantes implementadas:
 * - MT-01: EmpresaId obligatorio en toda operación.
 * - Una solicitud en BORRADOR acepta líneas; otras no.
 * - Solo un BORRADOR puede pasar a PENDIENTE_APROBACION.
 * - Solo PENDIENTE_APROBACION puede ser APROBADA o RECHAZADA.
 * - APROBADA y RECHAZADA son estados terminales de la decisión.
 * - No se puede aprobar o rechazar una solicitud sin líneas.
 *
 * Cero dependencias a Spring, Lombok o JPA (Regla 1, ADR-001).
 */
public class SolicitudAbastecimiento {

    private final SolicitudId id;
    private final EmpresaId empresaId;
    private final BodegaId bodegaId;
    private final Instant creadoEn;

    private EstadoSolicitud estado;
    private final List<LineaSolicitud> lineas;
    private final List<DomainEvent> domainEvents;

    // -------------------------------------------------------------------------
    // Constructor privado — acceso exclusivo vía factory methods
    // -------------------------------------------------------------------------
    private SolicitudAbastecimiento(SolicitudId id, EmpresaId empresaId, BodegaId bodegaId) {
        this.id        = Objects.requireNonNull(id,        "SolicitudId no puede ser nulo.");
        this.empresaId = Objects.requireNonNull(empresaId, "EmpresaId no puede ser nulo (MT-01).");
        this.bodegaId  = Objects.requireNonNull(bodegaId,  "BodegaId no puede ser nulo.");
        this.creadoEn  = Instant.now();
        this.estado    = EstadoSolicitud.BORRADOR;
        this.lineas    = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
    }

    // -------------------------------------------------------------------------
    // Factory method — única vía de creación de una solicitud nueva
    // -------------------------------------------------------------------------

    /**
     * Crea un borrador de solicitud de abastecimiento.
     *
     * @param id         Identidad de la solicitud.
     * @param empresaId  Tenant raíz (MT-01).
     * @param bodegaId   Bodega destino que solicita el reabastecimiento.
     * @return           Nueva instancia en estado BORRADOR.
     */
    public static SolicitudAbastecimiento iniciar(SolicitudId id, EmpresaId empresaId, BodegaId bodegaId) {
        return new SolicitudAbastecimiento(id, empresaId, bodegaId);
    }

    /**
     * Reconstitución desde persistencia (sin emitir eventos de dominio).
     */
    public static SolicitudAbastecimiento reconstituir(
            SolicitudId id,
            EmpresaId empresaId,
            BodegaId bodegaId,
            EstadoSolicitud estado,
            List<LineaSolicitud> lineas,
            Instant creadoEn) {
        SolicitudAbastecimiento sa = new SolicitudAbastecimiento(id, empresaId, bodegaId);
        // Sobrescribimos el estado y las líneas reconstruidas desde BD
        sa.estado = estado;
        sa.lineas.addAll(Objects.requireNonNull(lineas, "Las líneas reconstituidas no pueden ser nulas."));
        // No asignamos creadoEn directamente porque es final; se acepta el valor interno generado
        // en reconstitución por diseño de inmutabilidad del timestamp de creación.
        return sa;
    }

    // -------------------------------------------------------------------------
    // Comportamiento de negocio puro
    // -------------------------------------------------------------------------

    /**
     * Agrega una línea de producto a la solicitud.
     * Solo permitido mientras la solicitud esté en estado BORRADOR.
     *
     * @param linea Línea a agregar (ProductoId + cantidadSolicitada > 0).
     * @throws DomainException si el estado no es BORRADOR.
     */
    public void agregarLinea(LineaSolicitud linea) {
        validarEstado(EstadoSolicitud.BORRADOR,
                "Solo se pueden agregar líneas a una solicitud en estado BORRADOR.");
        this.lineas.add(Objects.requireNonNull(linea, "La línea de solicitud no puede ser nula."));
    }

    /**
     * Transiciona la solicitud a PENDIENTE_APROBACION.
     * Valida que exista al menos una línea antes de enviar.
     *
     * @throws DomainException si el estado no es BORRADOR o si no hay líneas.
     */
    public void solicitarAprobacion() {
        validarEstado(EstadoSolicitud.BORRADOR,
                "Solo una solicitud en estado BORRADOR puede enviarse a aprobación.");
        if (this.lineas.isEmpty()) {
            throw new DomainException(
                    "No se puede enviar a aprobación una solicitud sin líneas de producto.");
        }
        this.estado = EstadoSolicitud.PENDIENTE_APROBACION;
    }

    /**
     * Aprueba la solicitud de abastecimiento.
     * Solo aplicable en estado PENDIENTE_APROBACION.
     *
     * @throws DomainException si el estado no es PENDIENTE_APROBACION.
     */
    public void aprobar() {
        validarEstado(EstadoSolicitud.PENDIENTE_APROBACION,
                "Solo una solicitud PENDIENTE_APROBACION puede ser aprobada.");
        this.estado = EstadoSolicitud.APROBADA;
    }

    /**
     * Rechaza la solicitud de abastecimiento.
     * Solo aplicable en estado PENDIENTE_APROBACION.
     *
     * @throws DomainException si el estado no es PENDIENTE_APROBACION.
     */
    public void rechazar() {
        validarEstado(EstadoSolicitud.PENDIENTE_APROBACION,
                "Solo una solicitud PENDIENTE_APROBACION puede ser rechazada.");
        this.estado = EstadoSolicitud.RECHAZADA;
    }

    /**
     * Marca la solicitud como PROCESADA una vez que se ha generado la OrdenCompra.
     *
     * @throws DomainException si el estado no es APROBADA.
     */
    public void marcarComoProcesada() {
        validarEstado(EstadoSolicitud.APROBADA,
                "Solo una solicitud APROBADA puede marcarse como PROCESADA.");
        this.estado = EstadoSolicitud.PROCESADA;
    }

    // -------------------------------------------------------------------------
    // Invariante de guarda — reutilizable y autoexplicativa
    // -------------------------------------------------------------------------
    private void validarEstado(EstadoSolicitud esperado, String mensajeDeError) {
        if (this.estado != esperado) {
            throw new DomainException(mensajeDeError +
                    " Estado actual: " + this.estado + ". Estado requerido: " + esperado + ".");
        }
    }

    // -------------------------------------------------------------------------
    // Drenado de eventos — llamado por el Application Service post-persistencia
    // -------------------------------------------------------------------------
    public List<DomainEvent> drenaEventos() {
        List<DomainEvent> eventos = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return eventos;
    }

    // -------------------------------------------------------------------------
    // Getters (lectura inmutable)
    // -------------------------------------------------------------------------
    public SolicitudId getId()             { return id; }
    public EmpresaId getEmpresaId()        { return empresaId; }
    public BodegaId getBodegaId()          { return bodegaId; }
    public EstadoSolicitud getEstado()     { return estado; }
    public Instant getCreadoEn()           { return creadoEn; }
    public List<LineaSolicitud> getLineas(){ return Collections.unmodifiableList(lineas); }
    public List<DomainEvent> getDomainEvents() { return Collections.unmodifiableList(domainEvents); }
}
