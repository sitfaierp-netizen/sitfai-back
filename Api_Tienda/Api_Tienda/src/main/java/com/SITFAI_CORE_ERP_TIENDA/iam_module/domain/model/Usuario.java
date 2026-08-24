package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event.*;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.exception.UsuarioInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Email;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.Username;
import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.UsuarioId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root: Usuario (Identity & Access Management - IAM).
 * <p>
 * Representa la identidad corporativa y control de acceso de un empleado asignado a un tenant (EmpresaId).
 * Cumple con MT-01, aislamiento estricto de capas y reglas de negocio.
 */
public class Usuario {

    private final UsuarioId id;
    private final EmpresaId empresaId;
    private final Username username;
    private final Email email;
    private RolUsuario rol;
    private EstadoUsuario estado;
    private final Instant creadoEn;
    private Instant actualizadoEn;
    private boolean activo = true;
    private Instant deletedAt;
    private String deletedBy;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Usuario(
            UsuarioId id,
            EmpresaId empresaId,
            Username username,
            Email email,
            RolUsuario rol,
            EstadoUsuario estado,
            Instant creadoEn,
            Instant actualizadoEn
    ) {
        this(id, empresaId, username, email, rol, estado, creadoEn, actualizadoEn, true, null, null);
    }

    private Usuario(
            UsuarioId id,
            EmpresaId empresaId,
            Username username,
            Email email,
            RolUsuario rol,
            EstadoUsuario estado,
            Instant creadoEn,
            Instant actualizadoEn,
            boolean activo,
            Instant deletedAt,
            String deletedBy
    ) {
        this.id = Objects.requireNonNull(id, "El ID de usuario no puede ser nulo.");
        this.empresaId = Objects.requireNonNull(empresaId, "El EmpresaId (Tenant) no puede ser nulo.");
        this.username = Objects.requireNonNull(username, "El Username no puede ser nulo.");
        this.email = Objects.requireNonNull(email, "El Email no puede ser nulo.");
        this.rol = Objects.requireNonNull(rol, "El RolUsuario no puede ser nulo.");
        this.estado = Objects.requireNonNull(estado, "El EstadoUsuario no puede ser nulo.");
        this.creadoEn = Objects.requireNonNull(creadoEn, "La fecha de creación no puede ser nula.");
        this.actualizadoEn = Objects.requireNonNull(actualizadoEn, "La fecha de actualización no puede ser nula.");
        this.activo = activo;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    /**
     * Factory Method de Creación / Registro de un nuevo Usuario en el sistema.
     * Invariante: Todo usuario nace con estado ACTIVO y debe tener un rol válido asignado.
     */
    public static Usuario registrar(
            UsuarioId id,
            EmpresaId empresaId,
            Username username,
            Email email,
            RolUsuario rol
    ) {
        Instant ahora = Instant.now();
        Usuario usuario = new Usuario(
                id,
                empresaId,
                username,
                email,
                rol,
                EstadoUsuario.ACTIVO,
                ahora,
                ahora
        );

        usuario.domainEvents.add(new UsuarioRegistradoEvent(
                id,
                empresaId,
                username.valor(),
                email.valor(),
                rol.name(),
                ahora
        ));

        return usuario;
    }

    /**
     * Desactiva / inhabilita al usuario en la plataforma.
     */
    public void desactivar(String motivo) {
        if (this.estado == EstadoUsuario.INACTIVO) {
            throw new UsuarioInvalidoException("El usuario '" + this.username.valor() + "' ya se encuentra inactivo.");
        }
        this.estado = EstadoUsuario.INACTIVO;
        this.actualizadoEn = Instant.now();

        this.domainEvents.add(new UsuarioDesactivadoEvent(
                this.id,
                this.empresaId,
                motivo != null ? motivo.trim() : "Baja administrativa",
                this.actualizadoEn
        ));
    }

    /**
     * Desactiva al usuario sin motivo explícito.
     */
    public void desactivar() {
        desactivar("Desactivación por el administrador");
    }

    /**
     * Reactiva al usuario inhabilitado.
     */
    public void reactivar() {
        if (this.estado == EstadoUsuario.ACTIVO) {
            throw new UsuarioInvalidoException("El usuario '" + this.username.valor() + "' ya se encuentra activo.");
        }
        this.estado = EstadoUsuario.ACTIVO;
        this.actualizadoEn = Instant.now();

        this.domainEvents.add(new UsuarioReactivadoEvent(
                this.id,
                this.empresaId,
                this.actualizadoEn
        ));
    }

    /**
     * Modifica el rol asignado al usuario.
     * Invariante: No se puede cambiar el rol a un usuario inactivo, ni asignar el mismo rol actual.
     */
    public void cambiarRol(RolUsuario nuevoRol) {
        if (nuevoRol == null) {
            throw new UsuarioInvalidoException("El nuevo rol no puede ser nulo.");
        }
        if (this.estado == EstadoUsuario.INACTIVO) {
            throw new UsuarioInvalidoException("No se puede modificar el rol de un usuario inactivo: " + this.username.valor());
        }
        if (this.rol == nuevoRol) {
            throw new UsuarioInvalidoException("El usuario '" + this.username.valor() + "' ya tiene asignado el rol " + nuevoRol);
        }

        RolUsuario rolAnterior = this.rol;
        this.rol = nuevoRol;
        this.actualizadoEn = Instant.now();

        this.domainEvents.add(new RolUsuarioModificadoEvent(
                this.id,
                this.empresaId,
                rolAnterior.name(),
                nuevoRol.name(),
                this.actualizadoEn
        ));
    }

    /**
     * Factory Method de Reconstitución desde la Capa de Persistencia (sin disparar Domain Events).
     */
    public static Usuario reconstituir(
            UsuarioId id,
            EmpresaId empresaId,
            Username username,
            Email email,
            RolUsuario rol,
            EstadoUsuario estado,
            Instant creadoEn,
            Instant actualizadoEn,
            boolean activo,
            Instant deletedAt,
            String deletedBy
    ) {
        return new Usuario(
                id,
                empresaId,
                username,
                email,
                rol,
                estado,
                creadoEn,
                actualizadoEn,
                activo,
                deletedAt,
                deletedBy
        );
    }

    /**
     * Da de baja lógicamente al usuario (Soft Delete).
     */
    public void darDeBaja(String actorId) {
        if (!this.activo) {
            return;
        }
        Objects.requireNonNull(actorId, "El actorId no puede ser null.");
        this.activo = false;
        this.deletedAt = Instant.now();
        this.deletedBy = actorId;
        this.actualizadoEn = Instant.now();
    }

    // Getters inmutables
    public UsuarioId getId() {
        return id;
    }

    public EmpresaId getEmpresaId() {
        return empresaId;
    }

    public Username getUsername() {
        return username;
    }

    public Email getEmail() {
        return email;
    }

    public RolUsuario getRol() {
        return rol;
    }

    public EstadoUsuario getEstado() {
        return estado;
    }

    public boolean estaActivo() {
        return this.estado == EstadoUsuario.ACTIVO;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
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

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(events);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", empresaId=" + empresaId +
                ", username=" + username +
                ", email=" + email +
                ", rol=" + rol +
                ", estado=" + estado +
                '}';
    }
}
