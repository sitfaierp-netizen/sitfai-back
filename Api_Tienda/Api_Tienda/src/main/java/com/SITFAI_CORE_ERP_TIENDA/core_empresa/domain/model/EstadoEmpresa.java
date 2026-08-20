package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model;

/**
 * Estados del ciclo de vida de una Empresa (Tenant Raíz / EMP-04).
 */
public enum EstadoEmpresa {
    /**
     * Empresa operativa y con transacciones habilitadas (EMP-04).
     */
    ACTIVA,

    /**
     * Empresa suspendida administrativamente. No puede generar nuevas transacciones (EMP-06).
     */
    SUSPENDIDA,

    /**
     * Empresa dada de baja de forma terminal (EMP-04, EMP-07).
     */
    BAJA,

    /**
     * Empresa eliminada lógicamente.
     */
    ELIMINADO
}
