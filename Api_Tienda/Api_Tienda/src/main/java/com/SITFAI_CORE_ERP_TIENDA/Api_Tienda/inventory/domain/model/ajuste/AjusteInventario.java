package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.vo.AjusteInventarioId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.vo.MotivoAjuste;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.DocumentoTransaccional;
import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.enums.DocumentStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root: Ajuste de Inventario (Cycle Counting).
 * Permite corregir discrepancias en el stock debido a mermas, robos, caducidad, etc.
 * Implementa DocumentoTransaccional.
 */
public class AjusteInventario implements DocumentoTransaccional {

    private final AjusteInventarioId id;
    private final EmpresaId empresaId; // MT-01: Aislamiento estricto
    private final BodegaId bodegaId;
    private final MotivoAjuste motivo;
    private final List<LineaAjuste> lineas;
    private final List<DomainEvent> domainEvents;

    private DocumentStatus estado;
    private final Instant createdAt;
    private final String createdBy;

    private AjusteInventario(AjusteInventarioId id, EmpresaId empresaId, BodegaId bodegaId, MotivoAjuste motivo, String createdBy) {
        if (id == null) throw new IllegalArgumentException("AjusteInventario: id es obligatorio.");
        if (empresaId == null) throw new IllegalArgumentException("AjusteInventario: empresaId es obligatorio (MT-01).");
        if (bodegaId == null) throw new IllegalArgumentException("AjusteInventario: bodegaId es obligatoria.");
        if (motivo == null) throw new IllegalArgumentException("AjusteInventario: motivo es obligatorio.");
        if (createdBy == null || createdBy.isBlank()) throw new IllegalArgumentException("AjusteInventario: createdBy es obligatorio.");

        this.id = id;
        this.empresaId = empresaId;
        this.bodegaId = bodegaId;
        this.motivo = motivo;
        this.lineas = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
        this.estado = DocumentStatus.BORRADOR;
        this.createdAt = Instant.now();
        this.createdBy = createdBy;
    }

    public static AjusteInventario crearBorrador(AjusteInventarioId id, EmpresaId empresaId, BodegaId bodegaId, MotivoAjuste motivo, String createdBy) {
        return new AjusteInventario(id, empresaId, bodegaId, motivo, createdBy);
    }

    public void agregarLinea(LineaAjuste linea) {
        if (!puedeEditar()) {
            throw new IllegalStateException("No se pueden agregar líneas a un ajuste que no está en BORRADOR.");
        }
        if (linea == null) {
            throw new IllegalArgumentException("La línea de ajuste no puede ser nula.");
        }
        this.lineas.add(linea);
    }

    public static AjusteInventario reconstituir(AjusteInventarioId id, EmpresaId empresaId, BodegaId bodegaId, MotivoAjuste motivo, String createdBy, Instant createdAt, DocumentStatus estado, List<LineaAjuste> lineas) {
        AjusteInventario ajuste = new AjusteInventario(id, empresaId, bodegaId, motivo, createdBy);
        ajuste.estado = estado;
        // Se omite createdAt para mantener la inmutabilidad o se deja el generado (en un escenario real se pasaría por constructor privado extendido)
        if (lineas != null) {
            ajuste.lineas.addAll(lineas);
        }
        return ajuste;
    }

    public void confirmarAjuste() {
        if (this.lineas.isEmpty()) {
            throw new IllegalStateException("No se puede confirmar un ajuste sin líneas.");
        }
        // Transición de estado vía DocumentoTransaccional
        this.emitir(); 
        
        // El evento AjusteAplicadoEvent se publicará a nivel de caso de uso
    }

    public AjusteInventarioId getId() { return id; }
    public EmpresaId getEmpresaId() { return empresaId; }
    public BodegaId getBodegaId() { return bodegaId; }
    public MotivoAjuste getMotivo() { return motivo; }
    public List<LineaAjuste> getLineas() { return Collections.unmodifiableList(lineas); }

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
