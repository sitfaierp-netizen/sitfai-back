package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.RecepcionConfirmadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.OrdenCompraId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.RecepcionId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.DocumentoTransaccional;
import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.enums.DocumentStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root: Recepción de Inventario (Inbound Logistics).
 * <p>
 * Implementa DocumentoTransaccional. Contiene validaciones estrictas (MT-01, BOD-04, FEFO).
 */
public class Recepcion implements DocumentoTransaccional {
    private final RecepcionId id;
    private final EmpresaId empresaId; // MT-01: Aislamiento estricto
    private final BodegaId bodegaDestino;
    private final OrdenCompraId ordenCompraOrigen; // BOD-04: Documento Fuente obligatorio
    private final List<LineaRecepcion> lineas;
    private final List<DomainEvent> domainEvents;
    
    private DocumentStatus estado;
    private final Instant createdAt;
    private final String createdBy;

    private Recepcion(RecepcionId id, EmpresaId empresaId, BodegaId bodegaDestino, 
                      OrdenCompraId ordenCompraOrigen, String createdBy) {
        if (id == null) throw new IllegalArgumentException("RecepcionId es obligatorio.");
        if (empresaId == null) throw new IllegalArgumentException("EmpresaId es obligatorio (MT-01).");
        if (bodegaDestino == null) throw new IllegalArgumentException("BodegaId destino es obligatoria.");
        if (ordenCompraOrigen == null) throw new IllegalArgumentException("OrdenCompraId es obligatoria (BOD-04).");
        if (createdBy == null || createdBy.isBlank()) throw new IllegalArgumentException("CreatedBy es obligatorio.");

        this.id = id;
        this.empresaId = empresaId;
        this.bodegaDestino = bodegaDestino;
        this.ordenCompraOrigen = ordenCompraOrigen;
        this.lineas = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
        this.estado = DocumentStatus.BORRADOR;
        this.createdAt = Instant.now();
        this.createdBy = createdBy;
    }

    public static Recepcion crearBorrador(RecepcionId id, EmpresaId empresaId, BodegaId bodegaDestino, 
                                          OrdenCompraId ordenCompraOrigen, String createdBy) {
        return new Recepcion(id, empresaId, bodegaDestino, ordenCompraOrigen, createdBy);
    }

    public void agregarLinea(LineaRecepcion linea) {
        if (!puedeEditar()) {
            throw new IllegalStateException("No se pueden agregar líneas a una recepción que no está en BORRADOR.");
        }
        if (linea == null) {
            throw new IllegalArgumentException("La línea de recepción no puede ser nula.");
        }
        this.lineas.add(linea);
    }

    /**
     * Confirma la recepción. Emite el evento RecepcionConfirmadaEvent.
     */
    public void confirmarRecepcion() {
        if (this.lineas.isEmpty()) {
            throw new IllegalStateException("No se puede confirmar una recepción sin líneas.");
        }
        
        // Transición de estado vía DocumentoTransaccional
        this.emitir(); 
        
        // Emite evento de dominio
        this.domainEvents.add(RecepcionConfirmadaEvent.of(this.empresaId, this.id));
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }

    // --- Getters y Métodos de DocumentoTransaccional ---

    public RecepcionId getId() { return id; }
    public EmpresaId getEmpresaId() { return empresaId; }
    public BodegaId getBodegaDestino() { return bodegaDestino; }
    public OrdenCompraId getOrdenCompraOrigen() { return ordenCompraOrigen; }
    public List<LineaRecepcion> getLineas() { return Collections.unmodifiableList(lineas); }

    @Override
    public DocumentStatus getEstado() { return estado; }

    @Override
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public String getCreatedBy() { return createdBy; }

    @Override
    public void cambiarEstado(DocumentStatus nuevoEstado) {
        if (nuevoEstado == null) throw new IllegalArgumentException("El nuevo estado no puede ser nulo.");
        this.estado = nuevoEstado;
    }
}
