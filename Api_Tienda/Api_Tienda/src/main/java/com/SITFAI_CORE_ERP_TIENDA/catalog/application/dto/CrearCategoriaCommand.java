package com.SITFAI_CORE_ERP_TIENDA.catalog.application.dto;

public record CrearCategoriaCommand(
        String nombre,
        String categoriaPadreId
) {}
