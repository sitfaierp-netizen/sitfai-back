package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto;

/**
 * Web Request DTO: Payload HTTP para crear una Bodega.
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura (REGLA-5).
 * El {@code empresaId} se obtiene del header o token JWT de seguridad (MT-01, MT-06).
 */
public record CrearBodegaWebRequest(
        String sucursalId,
        String codigo,
        String nombre
) {}
