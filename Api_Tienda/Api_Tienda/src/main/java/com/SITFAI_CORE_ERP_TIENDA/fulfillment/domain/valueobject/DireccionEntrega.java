package com.SITFAI_CORE_ERP_TIENDA.fulfillment.domain.valueobject;

import java.util.Objects;

public record DireccionEntrega(String direccionLocal, String ciudad, String codigoPostal) {
    public DireccionEntrega {
        Objects.requireNonNull(direccionLocal, "La dirección local no puede ser nula");
        Objects.requireNonNull(ciudad, "La ciudad no puede ser nula");
        if (direccionLocal.isBlank() || ciudad.isBlank()) {
            throw new IllegalArgumentException("La dirección y la ciudad deben tener contenido válido");
        }
    }
}
