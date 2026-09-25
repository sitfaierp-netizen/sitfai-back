package com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo;

public record MotivoDevolucion(String valor) {
    public MotivoDevolucion {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El motivo de devolución no puede estar vacío");
        }
    }
    public static MotivoDevolucion de(String motivo) {
        return new MotivoDevolucion(motivo);
    }
}
