package com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.EmpresaCreadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.EmpresaDadaDeBajaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.EmpresaReactivadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.EmpresaSuspendidaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.SucursalActivadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.SucursalAgregadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.SucursalDesactivadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.EmpresaActualizadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.EmpresaEliminadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.SucursalActualizadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.event.SucursalEliminadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaInvalidaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.NombreEmpresa;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.Ruc;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.SucursalId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root: Empresa (Tenant Raíz / EMP-01 a EMP-07).
 * <p>
 * Representa la cúspide de la jerarquía multitenant del SaaS.
 * Encapsula la gestión de Sucursales (SUC-01 a SUC-06), garantizando la unicidad de código local,
 * las transiciones estrictas del ciclo de vida y la emisión de Eventos de Dominio inmutables.
 */
public class Empresa {

    private final EmpresaId id;
    private Ruc ruc;
    private NombreEmpresa nombre;
    private EstadoEmpresa estado;
    private final List<Sucursal> sucursales;
    private final List<DomainEvent> domainEvents;
    private final Instant creadoEn;
    private Instant actualizadoEn;
    private Long version;
    private boolean activo = true;
    private Instant deletedAt;
    private String deletedBy;

    private Empresa(
            EmpresaId id,
            Ruc ruc,
            NombreEmpresa nombre,
            EstadoEmpresa estado,
            List<Sucursal> sucursales,
            Instant creadoEn,
            Instant actualizadoEn,
            Long version
    ) {
        this.id = Objects.requireNonNull(id, "EmpresaId no puede ser null.");
        this.ruc = Objects.requireNonNull(ruc, "Ruc no puede ser null.");
        this.nombre = Objects.requireNonNull(nombre, "NombreEmpresa no puede ser null.");
        this.estado = Objects.requireNonNull(estado, "EstadoEmpresa no puede ser null.");
        this.sucursales = new ArrayList<>(Objects.requireNonNull(sucursales, "sucursales no puede ser null."));
        this.domainEvents = new ArrayList<>();
        this.creadoEn = Objects.requireNonNull(creadoEn, "creadoEn no puede ser null.");
        this.actualizadoEn = Objects.requireNonNull(actualizadoEn, "actualizadoEn no puede ser null.");
        this.version = version;
    }

    private Empresa(
            EmpresaId id,
            Ruc ruc,
            NombreEmpresa nombre,
            EstadoEmpresa estado,
            List<Sucursal> sucursales,
            Instant creadoEn,
            Instant actualizadoEn,
            Long version,
            boolean activo,
            Instant deletedAt,
            String deletedBy
    ) {
        this.id = Objects.requireNonNull(id, "EmpresaId no puede ser null.");
        this.ruc = Objects.requireNonNull(ruc, "Ruc no puede ser null.");
        this.nombre = Objects.requireNonNull(nombre, "NombreEmpresa no puede ser null.");
        this.estado = Objects.requireNonNull(estado, "EstadoEmpresa no puede ser null.");
        this.sucursales = new ArrayList<>(Objects.requireNonNull(sucursales, "sucursales no puede ser null."));
        this.domainEvents = new ArrayList<>();
        this.creadoEn = Objects.requireNonNull(creadoEn, "creadoEn no puede ser null.");
        this.actualizadoEn = Objects.requireNonNull(actualizadoEn, "actualizadoEn no puede ser null.");
        this.version = version;
        this.activo = activo;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    /**
     * Factory Method: Registra una nueva Empresa en estado ACTIVA (EMP-01, EMP-03).
     * Cumple con la regla EMP-03 creando la sucursal principal obligatoria para iniciar operaciones.
     * Emite {@link EmpresaCreadaEvent}.
     */
    public static Empresa registrar(
            EmpresaId id,
            Ruc ruc,
            NombreEmpresa nombre,
            String codigoSucursalPrincipal,
            String nombreSucursalPrincipal
    ) {
        Instant ahora = Instant.now();
        SucursalId sucursalPrincipalId = SucursalId.generar();
        String codSucursal = (codigoSucursalPrincipal != null && !codigoSucursalPrincipal.isBlank())
                ? codigoSucursalPrincipal.trim().toUpperCase()
                : "MATRIZ";
        String nomSucursal = (nombreSucursalPrincipal != null && !nombreSucursalPrincipal.isBlank())
                ? nombreSucursalPrincipal.trim()
                : "Sucursal Principal Matriz";

        Sucursal principal = Sucursal.crear(sucursalPrincipalId, codSucursal, nomSucursal);

        List<Sucursal> sucursalesIniciales = new ArrayList<>();
        sucursalesIniciales.add(principal);

        Empresa empresa = new Empresa(id, ruc, nombre, EstadoEmpresa.ACTIVA, sucursalesIniciales, ahora, ahora, 0L);

        empresa.domainEvents.add(EmpresaCreadaEvent.ahora(
                id,
                ruc,
                nombre,
                sucursalPrincipalId,
                codSucursal
        ));

        return empresa;
    }

    /**
     * Reconstituye una Empresa desde la capa de persistencia (sin emitir eventos ni validar transiciones de creación).
     */
    public static Empresa reconstituir(
            EmpresaId id,
            Ruc ruc,
            NombreEmpresa nombre,
            EstadoEmpresa estado,
            List<Sucursal> sucursales,
            Instant creadoEn,
            Instant actualizadoEn,
            Long version,
            boolean activo,
            Instant deletedAt,
            String deletedBy
    ) {
        return new Empresa(id, ruc, nombre, estado, sucursales, creadoEn, actualizadoEn, version, activo, deletedAt, deletedBy);
    }

    /**
     * Añade una nueva Sucursal a la Empresa.
     * <p>
     * Invariantes:
     * 1. Una Empresa BAJA o SUSPENDIDA no puede agregar sucursales (EMP-06).
     * 2. El código de sucursal debe ser único dentro de la Empresa (SUC-02).
     */
    public Sucursal agregarSucursal(String codigo, String nombreSucursal) {
        if (this.estado != EstadoEmpresa.ACTIVA) {
            throw new EmpresaInvalidaException(
                    "No se pueden agregar sucursales a una empresa en estado " + this.estado + " (EMP-06)."
            );
        }

        Objects.requireNonNull(codigo, "El código de sucursal no puede ser null.");
        String codigoNormalizado = codigo.trim().toUpperCase();

        boolean codigoDuplicado = this.sucursales.stream()
                .anyMatch(s -> s.getCodigo().equalsIgnoreCase(codigoNormalizado));

        if (codigoDuplicado) {
            throw new EmpresaInvalidaException(
                    "Ya existe una sucursal con el código '" + codigoNormalizado + "' en la empresa " + this.nombre.valor() + " (SUC-02)."
            );
        }

        SucursalId nuevaSucursalId = SucursalId.generar();
        Sucursal nuevaSucursal = Sucursal.crear(nuevaSucursalId, codigoNormalizado, nombreSucursal);
        this.sucursales.add(nuevaSucursal);
        this.actualizadoEn = Instant.now();

        this.domainEvents.add(SucursalAgregadaEvent.ahora(
                this.id,
                nuevaSucursalId,
                codigoNormalizado,
                nuevaSucursal.getNombre()
        ));

        return nuevaSucursal;
    }

    /**
     * Suspende operativamente la Empresa (EMP-04, EMP-06).
     * Emite {@link EmpresaSuspendidaEvent}.
     */
    public void suspender(String motivo) {
        if (this.estado == EstadoEmpresa.BAJA) {
            throw new EmpresaInvalidaException("No se puede suspender una empresa que ya está dada de BAJA (EMP-04).");
        }
        if (this.estado == EstadoEmpresa.SUSPENDIDA) {
            return;
        }

        Objects.requireNonNull(motivo, "El motivo de suspensión no puede ser null.");
        if (motivo.isBlank()) {
            throw new EmpresaInvalidaException("Debe especificar un motivo válido para suspender la empresa.");
        }

        this.estado = EstadoEmpresa.SUSPENDIDA;
        this.actualizadoEn = Instant.now();

        this.domainEvents.add(EmpresaSuspendidaEvent.ahora(this.id, motivo.trim()));
    }

    /**
     * Reactiva una Empresa suspendida (EMP-04).
     * Emite {@link EmpresaReactivadaEvent}.
     */
    public void reactivar() {
        if (this.estado == EstadoEmpresa.BAJA) {
            throw new EmpresaInvalidaException("No se puede reactivar una empresa dada de BAJA definitiva (EMP-04).");
        }
        if (this.estado == EstadoEmpresa.ACTIVA) {
            return;
        }

        this.estado = EstadoEmpresa.ACTIVA;
        this.actualizadoEn = Instant.now();

        this.domainEvents.add(EmpresaReactivadaEvent.ahora(this.id));
    }

    /**
     * Da de baja definitiva a la Empresa (EMP-04, EMP-07).
     * Transición terminal. Emite {@link EmpresaDadaDeBajaEvent}.
     */
    public void darDeBaja(String motivo) {
        if (this.estado == EstadoEmpresa.BAJA) {
            return;
        }

        Objects.requireNonNull(motivo, "El motivo de baja no puede ser null.");
        if (motivo.isBlank()) {
            throw new EmpresaInvalidaException("Debe especificar un motivo válido para dar de baja la empresa.");
        }

        this.estado = EstadoEmpresa.BAJA;
        // Desactiva automáticamente todas las sucursales
        this.sucursales.forEach(Sucursal::desactivar);
        this.actualizadoEn = Instant.now();

        this.domainEvents.add(EmpresaDadaDeBajaEvent.ahora(this.id, motivo.trim()));
    }

    /**
     * Aplica el Soft Delete a la Empresa.
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
        this.sucursales.forEach(s -> s.eliminar(actorId));
    }

    /**
     * Desactiva una sucursal específica (SUC-04, SUC-06).
     */
    public void desactivarSucursal(SucursalId sucursalId) {
        Sucursal sucursal = buscarSucursal(sucursalId);
        sucursal.desactivar();
        this.actualizadoEn = Instant.now();

        this.domainEvents.add(SucursalDesactivadaEvent.ahora(this.id, sucursalId));
    }

    /**
     * Activa una sucursal inactiva (SUC-04).
     */
    public void activarSucursal(SucursalId sucursalId) {
        if (this.estado != EstadoEmpresa.ACTIVA) {
            throw new EmpresaInvalidaException(
                    "No se pueden activar sucursales si la empresa no está ACTIVA (EMP-06)."
            );
        }
        Sucursal sucursal = buscarSucursal(sucursalId);
        sucursal.activar();
        this.actualizadoEn = Instant.now();

        this.domainEvents.add(SucursalActivadaEvent.ahora(this.id, sucursalId));
    }

    /**
     * Modifica el nombre de la empresa.
     */
    public void cambiarNombre(NombreEmpresa nuevoNombre) {
        this.nombre = Objects.requireNonNull(nuevoNombre, "Nuevo nombre no puede ser null.");
        this.actualizadoEn = Instant.now();
    }

    /**
     * Actualiza los datos de la empresa.
     */
    public void actualizarEmpresa(Ruc nuevoRuc, NombreEmpresa nuevoNombre) {
        this.ruc = Objects.requireNonNull(nuevoRuc, "Nuevo Ruc no puede ser null.");
        this.nombre = Objects.requireNonNull(nuevoNombre, "Nuevo nombre no puede ser null.");
        this.actualizadoEn = Instant.now();
        this.domainEvents.add(EmpresaActualizadaEvent.ahora(this.id, this.ruc, this.nombre));
    }

    /**
     * Elimina lógicamente la empresa.
     */
    public void eliminarEmpresa() {
        if (this.estado == EstadoEmpresa.ELIMINADO) {
            return;
        }
        this.estado = EstadoEmpresa.ELIMINADO;
        // Eliminar lógicamente todas las sucursales
        this.sucursales.forEach(Sucursal::eliminar);
        this.actualizadoEn = Instant.now();
        this.domainEvents.add(EmpresaEliminadaEvent.ahora(this.id));
    }

    /**
     * Actualiza los datos de una sucursal existente.
     */
    public void actualizarSucursal(SucursalId sucursalId, String codigo, String nombre) {
        Sucursal sucursal = buscarSucursal(sucursalId);
        sucursal.actualizar(codigo, nombre);
        this.actualizadoEn = Instant.now();
        this.domainEvents.add(SucursalActualizadaEvent.ahora(this.id, sucursalId, sucursal.getCodigo(), sucursal.getNombre()));
    }

    /**
     * Elimina lógicamente una sucursal existente.
     */
    public void eliminarSucursal(SucursalId sucursalId) {
        Sucursal sucursal = buscarSucursal(sucursalId);
        sucursal.eliminar();
        this.actualizadoEn = Instant.now();
        this.domainEvents.add(SucursalEliminadaEvent.ahora(this.id, sucursalId));
    }

    /**
     * Verifica si la empresa tiene al menos una sucursal activa para operar (EMP-03).
     */
    public boolean tieneSucursalActiva() {
        return this.sucursales.stream().anyMatch(Sucursal::isActiva);
    }

    private Sucursal buscarSucursal(SucursalId sucursalId) {
        Objects.requireNonNull(sucursalId, "SucursalId no puede ser null.");
        return this.sucursales.stream()
                .filter(s -> s.getId().equals(sucursalId))
                .findFirst()
                .orElseThrow(() -> new EmpresaInvalidaException(
                        "Sucursal con ID " + sucursalId.valor() + " no pertenece a la empresa " + this.id.valor()
                ));
    }

    /**
     * Extrae y limpia los eventos de dominio acumulados (Unit of Work).
     */
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> copia = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(copia);
    }

    // Getters inmutables
    public EmpresaId getId() {
        return id;
    }

    public Ruc getRuc() {
        return ruc;
    }

    public NombreEmpresa getNombre() {
        return nombre;
    }

    public EstadoEmpresa getEstado() {
        return estado;
    }

    public List<Sucursal> getSucursales() {
        return Collections.unmodifiableList(sucursales);
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }

    public Long getVersion() {
        return version;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Empresa empresa)) return false;
        return Objects.equals(id, empresa.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Empresa{" +
                "id=" + id +
                ", ruc=" + ruc +
                ", nombre=" + nombre +
                ", estado=" + estado +
                ", totalSucursales=" + sucursales.size() +
                '}';
    }
}
