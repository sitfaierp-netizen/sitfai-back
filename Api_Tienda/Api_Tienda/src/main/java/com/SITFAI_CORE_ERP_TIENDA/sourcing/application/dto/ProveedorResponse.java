package com.SITFAI_CORE_ERP_TIENDA.sourcing.application.dto;

import java.time.Instant;
import java.util.UUID;

/** DTO de respuesta para Proveedor. */
public record ProveedorResponse(
        UUID proveedorId,
        UUID empresaId,
        String ruc,
        String razonSocial,
        String emailContacto,
        String telefono,
        String direccion,
        String estado,
        Instant creadoEn
) {}
