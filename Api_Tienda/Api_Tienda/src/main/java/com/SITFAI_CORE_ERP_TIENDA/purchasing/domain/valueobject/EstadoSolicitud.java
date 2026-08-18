package com.SITFAI_CORE_ERP_TIENDA.purchasing.domain.valueobject;

/**
 * Enum de ciclo de vida de la SolicitudAbastecimiento.
 * Pertenece al Dominio — cero dependencias a frameworks.
 */
public enum EstadoSolicitud {

    /** Solicitud en preparación; permite agregar o quitar líneas. */
    BORRADOR,

    /** Solicitud enviada a aprobación; no acepta modificaciones. */
    PENDIENTE_APROBACION,

    /** Aprobada por el responsable; lista para generar OrdenCompra. */
    APROBADA,

    /** Rechazada por el responsable; estado terminal. */
    RECHAZADA,

    /** OrdenCompra generada a partir de esta solicitud; estado terminal. */
    PROCESADA
}
