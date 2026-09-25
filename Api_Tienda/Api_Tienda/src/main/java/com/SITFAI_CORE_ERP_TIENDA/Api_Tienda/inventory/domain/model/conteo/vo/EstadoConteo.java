package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo;

/**
 * Enumeración: Estados del ciclo de vida de un Conteo Cíclico en WMS.
 * <p>
 * Transiciones de estado:
 * <ul>
 *   <li>PLANIFICADO -> EN_EJECUCION (al registrar el primer conteo físico)</li>
 *   <li>EN_EJECUCION -> COMPLETADO (al finalizar si todas las cantidades físicas coinciden con las teóricas)</li>
 *   <li>EN_EJECUCION -> CON_DISCREPANCIAS (al finalizar si existen diferencias entre conteo físico y teórico)</li>
 * </ul>
 */
public enum EstadoConteo {

    PLANIFICADO,
    EN_EJECUCION,
    COMPLETADO,
    CON_DISCREPANCIAS;

    public boolean esFinalizado() {
        return this == COMPLETADO || this == CON_DISCREPANCIAS;
    }

    public boolean permiteRegistro() {
        return this == PLANIFICADO || this == EN_EJECUCION;
    }
}
