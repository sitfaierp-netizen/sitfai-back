package com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EventStatus;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.StoredEventId;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate Root: {@code StoredDomainEvent} en el Bounded Context de Auditoría y Trazabilidad (AUD-03).
 * <p>
 * Representa un registro notariado e inmutable de un Evento de Dominio producido en la arquitectura distribuida.
 * Garantiza:
 * <ul>
 *   <li><b>Aislamiento Multitenant (MT-01):</b> Pertenece inexorablemente a una sola {@link EmpresaId}.</li>
 *   <li><b>Inmutabilidad Histórica (AUD-03):</b> El nombre del evento, fecha de ocurrencia y el payload original
 *       son inmutables y sellados en el momento de la notarización.</li>
 *   <li><b>Soporte para Outbox Pattern:</b> Inicializado en estado {@link EventStatus#PENDIENTE} con capacidad
 *       de transicionar a {@link EventStatus#PROCESADO} o registrar fallos con {@link EventStatus#FALLIDO}.</li>
 * </ul>
 * <p>
 * Pureza absoluta: Cero dependencias técnicas de JPA, Spring o Jackson (REGLA-1, REGLA-3, MCP-01).
 */
public class StoredDomainEvent {

    private final StoredEventId id;
    private final EmpresaId empresaId;
    private final String nombreEvento;
    private final Instant ocurridoEn;
    private final String payload;

    private EventStatus estado;
    private String motivoFallo;
    private Instant procesadoEn;

    private StoredDomainEvent(
            StoredEventId id,
            EmpresaId empresaId,
            String nombreEvento,
            Instant ocurridoEn,
            String payload,
            EventStatus estado,
            String motivoFallo,
            Instant procesadoEn
    ) {
        this.id = Objects.requireNonNull(id, "StoredDomainEvent: StoredEventId no puede ser null.");
        this.empresaId = Objects.requireNonNull(empresaId, "StoredDomainEvent: EmpresaId no puede ser null (MT-01).");
        if (nombreEvento == null || nombreEvento.isBlank()) {
            throw new IllegalArgumentException("StoredDomainEvent: nombreEvento no puede ser nulo ni vacío.");
        }
        this.nombreEvento = nombreEvento.trim();
        this.ocurridoEn = Objects.requireNonNull(ocurridoEn, "StoredDomainEvent: ocurridoEn no puede ser null.");
        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException("StoredDomainEvent: payload no puede ser nulo ni vacío.");
        }
        this.payload = payload.trim();
        this.estado = Objects.requireNonNull(estado, "StoredDomainEvent: estado no puede ser null.");
        this.motivoFallo = motivoFallo != null ? motivoFallo.trim() : null;
        this.procesadoEn = procesadoEn;
    }

    /**
     * Factory Method principal: Notaría un nuevo evento de dominio en el almacén de auditoría.
     * <p>
     * Inicializa el evento en estado {@link EventStatus#PENDIENTE}, sellando su payload original
     * y garantizando el aislamiento del tenant (MT-01).
     *
     * @param id           Identificador único del evento almacenado.
     * @param empresaId    Identificador del tenant (MT-01).
     * @param nombreEvento Nombre canónico de la clase del evento (ej. ReservaRechazadaEvent).
     * @param ocurridoEn   Timestamp de ocurrencia original del evento.
     * @param payload      Representación JSON en formato String con el estado completo del evento.
     * @return Instancia del Aggregate Root sellada en estado PENDIENTE.
     */
    public static StoredDomainEvent notariar(
            StoredEventId id,
            EmpresaId empresaId,
            String nombreEvento,
            Instant ocurridoEn,
            String payload
    ) {
        return new StoredDomainEvent(
                id,
                empresaId,
                nombreEvento,
                ocurridoEn,
                payload,
                EventStatus.PENDIENTE,
                null,
                null
        );
    }

    /**
     * Factory Method conveniente: Notaría un nuevo evento generando un {@link StoredEventId} aleatorio.
     */
    public static StoredDomainEvent notariar(
            EmpresaId empresaId,
            String nombreEvento,
            Instant ocurridoEn,
            String payload
    ) {
        return notariar(StoredEventId.generar(), empresaId, nombreEvento, ocurridoEn, payload);
    }

    /**
     * Factory Method conveniente: Notaría un evento asumiendo la fecha actual del sistema.
     */
    public static StoredDomainEvent notariar(
            EmpresaId empresaId,
            String nombreEvento,
            String payload
    ) {
        return notariar(StoredEventId.generar(), empresaId, nombreEvento, Instant.now(), payload);
    }

    /**
     * Reconstituye una entidad de auditoría desde la capa de persistencia/infraestructura.
     * No ejecuta validaciones de creación inicial; restaura fielmente el estado histórico.
     */
    public static StoredDomainEvent reconstituir(
            StoredEventId id,
            EmpresaId empresaId,
            String nombreEvento,
            Instant ocurridoEn,
            String payload,
            EventStatus estado,
            String motivoFallo,
            Instant procesadoEn
    ) {
        return new StoredDomainEvent(
                id,
                empresaId,
                nombreEvento,
                ocurridoEn,
                payload,
                estado,
                motivoFallo,
                procesadoEn
        );
    }

    /**
     * Transiciona el estado a {@link EventStatus#PROCESADO}.
     * Limpia cualquier motivo de fallo previo y sella la fecha de procesamiento.
     */
    public void marcarProcesado() {
        marcarProcesado(Instant.now());
    }

    /**
     * Transiciona el estado a {@link EventStatus#PROCESADO} indicando la fecha exacta.
     *
     * @param fechaProcesado Timestamp de procesamiento efectivo.
     */
    public void marcarProcesado(Instant fechaProcesado) {
        this.estado = EventStatus.PROCESADO;
        this.procesadoEn = fechaProcesado != null ? fechaProcesado : Instant.now();
        this.motivoFallo = null;
    }

    /**
     * Transiciona el estado a {@link EventStatus#FALLIDO} registrando el motivo del error.
     *
     * @param motivo Descripción o causa del fallo reportado por el consumidor.
     */
    public void marcarFallido(String motivo) {
        marcarFallido(motivo, Instant.now());
    }

    /**
     * Transiciona el estado a {@link EventStatus#FALLIDO} registrando el motivo y la fecha del error.
     *
     * @param motivo         Descripción o causa del fallo.
     * @param fechaProcesado Timestamp en el que ocurrió la falla.
     */
    public void marcarFallido(String motivo, Instant fechaProcesado) {
        if (this.estado == EventStatus.PROCESADO) {
            throw new IllegalStateException("StoredDomainEvent: No se puede marcar como FALLIDO un evento que ya fue PROCESADO con éxito.");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("StoredDomainEvent: El motivo del fallo no puede ser nulo ni vacío.");
        }
        this.estado = EventStatus.FALLIDO;
        this.motivoFallo = motivo.trim();
        this.procesadoEn = fechaProcesado != null ? fechaProcesado : Instant.now();
    }

    // --- Consultas de Estado ---

    public boolean isPendiente() {
        return this.estado == EventStatus.PENDIENTE;
    }

    public boolean isProcesado() {
        return this.estado == EventStatus.PROCESADO;
    }

    public boolean isFallido() {
        return this.estado == EventStatus.FALLIDO;
    }

    // --- Getters Inmutables ---

    public StoredEventId getId() {
        return id;
    }

    public EmpresaId getEmpresaId() {
        return empresaId;
    }

    public String getNombreEvento() {
        return nombreEvento;
    }

    public Instant getOcurridoEn() {
        return ocurridoEn;
    }

    public String getPayload() {
        return payload;
    }

    public EventStatus getEstado() {
        return estado;
    }

    public String getMotivoFallo() {
        return motivoFallo;
    }

    public Instant getProcesadoEn() {
        return procesadoEn;
    }

    // --- Invariantes de Identidad DDD ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StoredDomainEvent that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "StoredDomainEvent{" +
                "id=" + id +
                ", empresaId=" + empresaId +
                ", nombreEvento='" + nombreEvento + '\'' +
                ", ocurridoEn=" + ocurridoEn +
                ", estado=" + estado +
                ", motivoFallo='" + motivoFallo + '\'' +
                ", procesadoEn=" + procesadoEn +
                '}';
    }
}
