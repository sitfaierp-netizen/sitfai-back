package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO de respuesta que representa el resultado de crear una Bodega.
 * <p>
 * Devuelto por {@code CrearBodegaUseCase} hacia el adaptador REST.
 * Contiene solo los datos necesarios para confirmar la operación —
 * el adaptador REST lo transforma a su propio Response DTO (Infraestructura).
 * <p>
 * Record inmutable (Java 25). Sin anotaciones Jackson — el adaptador REST
 * aplica la serialización en su propia capa.
 *
 * @param bodegaId    UUID de la Bodega recién creada.
 * @param empresaId   UUID del tenant propietario.
 * @param sucursalId  UUID de la Sucursal a la que pertenece.
 * @param codigo      Código asignado (normalizado — uppercase).
 * @param nombre      Nombre de la Bodega.
 * @param activa      Estado inicial (siempre {@code true} en creación).
 * @param creadoEn    Timestamp de creación.
 */
public record BodegaResponse(
        String bodegaId,
        String empresaId,
        String sucursalId,
        String codigo,
        String nombre,
        boolean activa,
        Instant creadoEn
) {}
