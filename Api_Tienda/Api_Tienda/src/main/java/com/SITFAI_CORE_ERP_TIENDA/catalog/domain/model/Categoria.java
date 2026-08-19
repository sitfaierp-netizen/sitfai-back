package com.SITFAI_CORE_ERP_TIENDA.catalog.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.CategoriaId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.EstadoCategoria;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate Root: Categoría del Catálogo.
 *
 * Reglas implementadas:
 * - El nombre no puede ser nulo ni vacío.
 * - Un categoría DESCONTINUADA no puede volver a ACTIVO.
 * - categoríaPadreId puede ser null (categoría raíz).
 * - Cero dependencias a frameworks (Regla 1, ADR-001).
 */
public class Categoria {

    private final CategoriaId categoriaId;
    private final EmpresaId empresaId;
    private final Instant creadoEn;

    private String nombre;
    private CategoriaId categoriaPadreId; // null → categoría raíz
    private EstadoCategoria estado;

    // -------------------------------------------------------------------------
    // Constructor privado
    // -------------------------------------------------------------------------
    private Categoria(CategoriaId categoriaId, EmpresaId empresaId, String nombre,
                      CategoriaId categoriaPadreId) {
        this.categoriaId     = Objects.requireNonNull(categoriaId, "CategoriaId no puede ser nulo.");
        this.empresaId       = Objects.requireNonNull(empresaId,   "EmpresaId no puede ser nulo (MT-01).");
        validarNombre(nombre);
        this.nombre          = nombre.trim();
        this.categoriaPadreId = categoriaPadreId; // nullable — categoría raíz
        this.estado          = EstadoCategoria.ACTIVO;
        this.creadoEn        = Instant.now();
    }

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------
    public static Categoria crear(CategoriaId id, EmpresaId empresaId,
                                  String nombre, CategoriaId padreId) {
        return new Categoria(id, empresaId, nombre, padreId);
    }

    public static Categoria reconstituir(CategoriaId id, EmpresaId empresaId,
                                         String nombre, CategoriaId padreId,
                                         EstadoCategoria estado, Instant creadoEn) {
        Categoria c = new Categoria(id, empresaId, nombre, padreId);
        c.estado = estado;
        return c;
    }

    // -------------------------------------------------------------------------
    // Comportamiento de negocio
    // -------------------------------------------------------------------------

    public void renombrar(String nuevoNombre) {
        validarNombre(nuevoNombre);
        this.nombre = nuevoNombre.trim();
    }

    public void cambiarEstado(EstadoCategoria nuevoEstado) {
        if (this.estado == EstadoCategoria.DESCONTINUADO
                && nuevoEstado == EstadoCategoria.ACTIVO) {
            throw new DomainException(
                    "Una Categoría DESCONTINUADA no puede volver a estado ACTIVO.");
        }
        this.estado = nuevoEstado;
    }

    // -------------------------------------------------------------------------
    // Validaciones privadas
    // -------------------------------------------------------------------------
    private void validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("El nombre de la Categoría no puede estar vacío.");
        }
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------
    public CategoriaId getCategoriaId()      { return categoriaId; }
    public EmpresaId getEmpresaId()          { return empresaId; }
    public String getNombre()                { return nombre; }
    public CategoriaId getCategoriaPadreId() { return categoriaPadreId; }
    public EstadoCategoria getEstado()       { return estado; }
    public Instant getCreadoEn()             { return creadoEn; }
}
