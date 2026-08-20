package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaInvalidaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.SucursalId;

import java.time.Instant;
import java.util.Objects;

/**
 * Entidad de Dominio: Sucursal (SUC-01 a SUC-06).
 * <p>
 * Entidad interna gestionada exclusivamente a través del Agregado Raíz {@link Empresa}.
 * Controla su propio estado operativo (ACTIVA, INACTIVA) y su código identificador local.
 */
public class Sucursal {

    private final SucursalId id;
    private String codigo;
    private String nombre;
    private EstadoSucursal estado;
    private final Instant creadoEn;
    private Instant actualizadoEn;
    private boolean activo = true;
    private Instant deletedAt;
    private String deletedBy;

    private Sucursal(SucursalId id, String codigo, String nombre, EstadoSucursal estado, Instant creadoEn, Instant actualizadoEn) {
        this.id = Objects.requireNonNull(id, "SucursalId no puede ser null.");
        this.codigo = validarCodigo(codigo);
        this.nombre = validarNombre(nombre);
        this.estado = Objects.requireNonNull(estado, "EstadoSucursal no puede ser null.");
        this.creadoEn = Objects.requireNonNull(creadoEn, "creadoEn no puede ser null.");
        this.actualizadoEn = Objects.requireNonNull(actualizadoEn, "actualizadoEn no puede ser null.");
    }

    private Sucursal(SucursalId id, String codigo, String nombre, EstadoSucursal estado, Instant creadoEn, Instant actualizadoEn, boolean activo, Instant deletedAt, String deletedBy) {
        this.id = Objects.requireNonNull(id, "SucursalId no puede ser null.");
        this.codigo = validarCodigo(codigo);
        this.nombre = validarNombre(nombre);
        this.estado = Objects.requireNonNull(estado, "EstadoSucursal no puede ser null.");
        this.creadoEn = Objects.requireNonNull(creadoEn, "creadoEn no puede ser null.");
        this.actualizadoEn = Objects.requireNonNull(actualizadoEn, "actualizadoEn no puede ser null.");
        this.activo = activo;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    /**
     * Factory method para crear una nueva Sucursal activa.
     */
    public static Sucursal crear(SucursalId id, String codigo, String nombre) {
        Instant ahora = Instant.now();
        return new Sucursal(id, codigo, nombre, EstadoSucursal.ACTIVA, ahora, ahora);
    }

    /**
     * Reconstitución desde la capa de persistencia (sin lógica de creación).
     */
    public static Sucursal reconstituir(
            SucursalId id,
            String codigo,
            String nombre,
            EstadoSucursal estado,
            Instant creadoEn,
            Instant actualizadoEn,
            boolean activo,
            Instant deletedAt,
            String deletedBy
    ) {
        return new Sucursal(id, codigo, nombre, estado, creadoEn, actualizadoEn, activo, deletedAt, deletedBy);
    }

    /**
     * Desactiva la sucursal (SUC-04, SUC-06).
     */
    public void desactivar() {
        if (this.estado == EstadoSucursal.INACTIVA) {
            return;
        }
        this.estado = EstadoSucursal.INACTIVA;
        this.actualizadoEn = Instant.now();
    }

    /**
     * Activa la sucursal (SUC-04).
     */
    public void activar() {
        if (this.estado == EstadoSucursal.ACTIVA) {
            return;
        }
        this.estado = EstadoSucursal.ACTIVA;
        this.actualizadoEn = Instant.now();
    }

    /**
     * Elimina logicamente la sucursal.
     */
    public void eliminar(String actorId) {
        if (!this.activo) {
            return;
        }
        Objects.requireNonNull(actorId, "El actorId no puede ser null.");
        this.activo = false;
        this.deletedAt = Instant.now();
        this.deletedBy = actorId;
        this.actualizadoEn = Instant.now();
    }

    /**
     * Actualiza el nombre descriptivo de la sucursal.
     */
    public void cambiarNombre(String nuevoNombre) {
        this.nombre = validarNombre(nuevoNombre);
        this.actualizadoEn = Instant.now();
    }

    /**
     * Actualiza los datos de la sucursal (código y nombre).
     */
    public void actualizar(String codigo, String nombre) {
        this.codigo = validarCodigo(codigo);
        this.nombre = validarNombre(nombre);
        this.actualizadoEn = Instant.now();
    }

    /**
     * Elimina lógicamente la sucursal.
     */
    public void eliminar() {
        if (this.estado == EstadoSucursal.ELIMINADO) {
            return;
        }
        this.estado = EstadoSucursal.ELIMINADO;
        this.actualizadoEn = Instant.now();
    }

    public boolean isActiva() {
        return this.estado == EstadoSucursal.ACTIVA;
    }

    public boolean isActivo() {
        return activo;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    private static String validarCodigo(String codigo) {
        Objects.requireNonNull(codigo, "El código de sucursal no puede ser null.");
        String limpio = codigo.trim().toUpperCase();
        if (limpio.isBlank()) {
            throw new EmpresaInvalidaException("El código de sucursal no puede estar vacío.");
        }
        if (limpio.length() < 2 || limpio.length() > 20) {
            throw new EmpresaInvalidaException("El código de sucursal debe tener entre 2 y 20 caracteres.");
        }
        return limpio;
    }

    private static String validarNombre(String nombre) {
        Objects.requireNonNull(nombre, "El nombre de sucursal no puede ser null.");
        String limpio = nombre.trim();
        if (limpio.isBlank()) {
            throw new EmpresaInvalidaException("El nombre de sucursal no puede estar vacío.");
        }
        if (limpio.length() < 2 || limpio.length() > 100) {
            throw new EmpresaInvalidaException("El nombre de sucursal debe tener entre 2 y 100 caracteres.");
        }
        return limpio;
    }

    // Getters inmutables
    public SucursalId getId() {
        return id;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public EstadoSucursal getEstado() {
        return estado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sucursal sucursal)) return false;
        return Objects.equals(id, sucursal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Sucursal{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", estado=" + estado +
                '}';
    }
}
