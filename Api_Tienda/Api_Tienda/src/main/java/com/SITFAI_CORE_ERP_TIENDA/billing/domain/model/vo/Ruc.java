package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.vo;

public record Ruc(String valor) {
    public Ruc {
        if (valor == null || valor.trim().isEmpty()) {
            throw new IllegalArgumentException("El RUC no puede estar vacío");
        }
        if (!valor.matches("\\d{13}")) {
            throw new IllegalArgumentException("El RUC debe contener exactamente 13 dígitos numéricos");
        }
    }
}
