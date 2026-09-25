package com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo;

import java.util.UUID;

public record DocumentoFuenteId(UUID valor) {
    public DocumentoFuenteId {
        if (valor == null) throw new IllegalArgumentException("El documento fuente es obligatorio (Regla BOD-04)");
    }
    public static DocumentoFuenteId de(UUID id) {
        return new DocumentoFuenteId(id);
    }
}
