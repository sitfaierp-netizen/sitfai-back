package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import java.util.UUID;

/**
 * Command: Datos de entrada para aprobar una SolicitudAbastecimiento.
 * El empresaId proviene del token JWT (MT-01) — nunca del payload del cliente.
 */
public record AprobarSolicitudCommand(
        UUID solicitudId,
        UUID empresaId
) {}
