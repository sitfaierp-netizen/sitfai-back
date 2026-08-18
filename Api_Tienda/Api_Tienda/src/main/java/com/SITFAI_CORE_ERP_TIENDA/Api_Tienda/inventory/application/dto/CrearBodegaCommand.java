package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto;

/**
 * Comando para crear una nueva Bodega en el sistema.
 * <p>
 * Record puro — inmutable por diseño (Java 25). Sin anotaciones de validación
 * web (@Valid, @NotNull) ni de serialización (Jackson). Esas responsabilidades
 * pertenecen a la capa de Infraestructura (adaptador REST).
 * <p>
 * La capa de Aplicación valida la coherencia de negocio; la capa de Infra valida
 * el formato HTTP del request.
 * <p>
 * Reglas validadas: REGLA-2 (dto en application/dto), BOD-01, BOD-02, MT-01.
 *
 * @param empresaId   UUID del tenant — extraído del JWT por el adaptador REST (MT-01, MT-06).
 * @param sucursalId  UUID de la Sucursal a la que pertenecerá la Bodega (BOD-01).
 * @param codigo      Código único de la Bodega dentro de la Sucursal (BOD-02).
 * @param nombre      Nombre descriptivo de la Bodega.
 */
public record CrearBodegaCommand(
        String empresaId,
        String sucursalId,
        String codigo,
        String nombre
) {
    /**
     * Validación fail-fast en el propio Command — antes de llegar al Use Case.
     */
    public CrearBodegaCommand {
        if (empresaId == null || empresaId.isBlank())
            throw new IllegalArgumentException("CrearBodegaCommand: empresaId es obligatorio (MT-01).");
        if (sucursalId == null || sucursalId.isBlank())
            throw new IllegalArgumentException("CrearBodegaCommand: sucursalId es obligatorio (BOD-01).");
        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("CrearBodegaCommand: codigo es obligatorio (BOD-02).");
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("CrearBodegaCommand: nombre es obligatorio.");
    }
}
