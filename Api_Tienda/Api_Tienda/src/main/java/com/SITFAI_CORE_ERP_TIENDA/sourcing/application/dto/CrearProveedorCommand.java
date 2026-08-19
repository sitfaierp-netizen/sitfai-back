package com.SITFAI_CORE_ERP_TIENDA.sourcing.application.dto;

/**
 * Command para crear un Proveedor.
 * PROHIBIDO incluir empresaId — se extrae del JWT (MT-01).
 */
public record CrearProveedorCommand(
        String ruc,
        String razonSocial,
        String emailContacto,
        String telefono,
        String direccion,
        Integer plazoEntregaDias
) {}
