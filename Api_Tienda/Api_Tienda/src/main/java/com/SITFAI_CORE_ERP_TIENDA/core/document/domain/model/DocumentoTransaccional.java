package com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.enums.DocumentStatus;
import com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception.DocumentStateException;

import java.time.Instant;

public interface DocumentoTransaccional {

    DocumentStatus getEstado();
    
    // Auditoría
    Instant getCreatedAt();
    String getCreatedBy();

    default boolean puedeEditar() {
        return getEstado() == DocumentStatus.BORRADOR;
    }

    default void emitir() {
        if (getEstado() != DocumentStatus.BORRADOR) {
            throw new DocumentStateException("Solo se puede emitir un documento en estado BORRADOR.");
        }
        cambiarEstado(DocumentStatus.EMITIDO);
    }

    default void anular() {
        if (getEstado() != DocumentStatus.EMITIDO) {
            throw new DocumentStateException("Solo se puede anular un documento en estado EMITIDO.");
        }
        cambiarEstado(DocumentStatus.ANULADO);
    }
    
    // Método abstracto interno para ser implementado por la entidad concreta
    void cambiarEstado(DocumentStatus nuevoEstado);
}
