package com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;

public record CrearProveedorRequest(
        @NotBlank(message = "El RUC es obligatorio")
        String ruc,
        @NotBlank(message = "La razón social es obligatoria")
        String razonSocial,
        @Email(message = "Email inválido")
        String emailContacto,
        @NotBlank(message = "El teléfono es obligatorio")
        String telefono,
        @NotBlank(message = "La dirección es obligatoria")
        String direccion,
        @NotNull(message = "El plazo de entrega es obligatorio")
        @Min(value = 0, message = "El plazo de entrega no puede ser negativo")
        Integer plazoEntregaDias
) {}
