package com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.StoredDomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.EventStatus;
import com.SITFAI_CORE_ERP_TIENDA.core_audit.domain.model.eventstore.vo.StoredEventId;

import java.util.List;
import java.util.Optional;

/**
 * Output Port: Repositorio de persistencia del Event Store (AUD-03).
 * <p>
 * Regla MT-01 (Inquebrantable): Toda firma de consulta exige obligatoriamente {@link EmpresaId}
 * para erradicar cualquier fuga o contaminación cruzada de auditoría entre inquilinos.
 * <p>
 * Pure Java Interface: Cero imports de Spring, JPA o frameworks técnicos (Regla 1 Clean Architecture).
 */
public interface EventStoreRepository {

    /**
     * Persiste o actualiza el estado de un evento de dominio notariado en el Event Store.
     *
     * @param evento Instancia del agregado a almacenar.
     * @return Instancia almacenada del evento.
     */
    StoredDomainEvent guardar(StoredDomainEvent evento);

    /**
     * Busca un evento específico por su identificador global y el tenant propietario (MT-01).
     *
     * @param id        Identificador único del evento almacenado.
     * @param empresaId Identificador del tenant para aislamiento estricto.
     * @return Optional con el evento encontrado o vacío si no existe en ese tenant.
     */
    Optional<StoredDomainEvent> buscarPorId(StoredEventId id, EmpresaId empresaId);

    /**
     * Obtiene todos los eventos de auditoría registrados para un inquilino específico (MT-01).
     *
     * @param empresaId Identificador del tenant.
     * @return Lista de eventos del tenant.
     */
    List<StoredDomainEvent> buscarPorEmpresa(EmpresaId empresaId);

    /**
     * Obtiene los eventos de un tenant filtrados por su estado de procesamiento (útil para el Outbox Poller).
     *
     * @param empresaId Identificador del tenant (MT-01).
     * @param estado    Estado del ciclo de vida (ej. PENDIENTE, FALLIDO).
     * @return Lista de eventos coincidentes.
     */
    List<StoredDomainEvent> buscarPorEmpresaYEstado(EmpresaId empresaId, EventStatus estado);

    /**
     * Obtiene los eventos de un tenant filtrados por el tipo o nombre canónico de evento.
     *
     * @param empresaId    Identificador del tenant (MT-01).
     * @param nombreEvento Nombre del evento de dominio.
     * @return Lista de eventos coincidentes.
     */
    List<StoredDomainEvent> buscarPorEmpresaYNombreEvento(EmpresaId empresaId, String nombreEvento);

    /**
     * Obtiene un lote de eventos en estado PENDIENTE para procesamiento asíncrono (Outbox Pattern).
     *
     * @param limite Cantidad máxima de eventos a recuperar.
     * @return Lista de eventos pendientes ordenados por fecha de ocurrencia ascendente.
     */
    List<StoredDomainEvent> buscarPendientes(int limite);
}
