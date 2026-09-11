package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum ModuloSistema {
    DASHBOARD, 
    EMPRESAS, 
    IAM, 
    VENTAS_PEDIDOS, 
    INVENTARIO, 
    FACTURACION, 
    POS;

    @JsonCreator
    public static ModuloSistema fromString(String value) {
        if (value == null) {
            return null;
        }
        
        String normalized = value.toUpperCase()
                .replace(" Y ", "_")
                .replace(" ", "_");
                
        if (normalized.contains("VENTAS") && normalized.contains("PEDIDOS")) {
            return VENTAS_PEDIDOS;
        }
        if (normalized.contains("INVENTARIO")) {
            return INVENTARIO;
        }
        
        for (ModuloSistema m : ModuloSistema.values()) {
            if (m.name().equals(normalized)) {
                return m;
            }
        }
        
        throw new IllegalArgumentException("Invalid ModuloSistema: " + value);
    }
}