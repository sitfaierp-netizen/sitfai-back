package com.SITFAI_CORE_ERP_TIENDA.billing.domain.valueobject;

import java.time.LocalDate;

/**
 * Value Object: Resolución DIAN de facturación.
 * Define prefijo, rango de numeración y vigencia.
 */
public record ResolucionDian(
        String prefijo,
        long rangoInicial,
        long rangoFinal,
        LocalDate vigenciaHasta
) {
    public ResolucionDian {
        if (rangoInicial <= 0 || rangoFinal < rangoInicial) {
            throw new IllegalArgumentException("Rango de resolución inválido");
        }
        if (vigenciaHasta == null) {
            throw new IllegalArgumentException("La vigencia es obligatoria");
        }
    }

    public boolean estaExpirada() {
        return LocalDate.now().isAfter(vigenciaHasta);
    }
    
    public boolean incluyeNumero(long numero) {
        return numero >= rangoInicial && numero <= rangoFinal;
    }
}
