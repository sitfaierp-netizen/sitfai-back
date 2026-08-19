package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.Objects;

/**
 * Entidad JPA para la persistencia del Agregado Usuario en la tabla iam_usuario.
 * Aislada completamente del Dominio. No almacena contraseñas (Protocolo 4).
 */
@Entity
@Table(name = "iam_usuario")
@SQLDelete(sql = "UPDATE iam_usuario SET activo = false, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("activo = true")
public class UsuarioJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "empresa_id", nullable = false, length = 36)
    private String empresaId;

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "email", nullable = false, length = 150)
    private String email;

    @Column(name = "rol", nullable = false, length = 30)
    private String rol;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by", length = 36)
    private String deletedBy;

    public UsuarioJpaEntity() {
    }

    public UsuarioJpaEntity(
            String id,
            String empresaId,
            String username,
            String email,
            String rol,
            String estado,
            Instant creadoEn,
            Instant actualizadoEn,
            boolean activo,
            Instant deletedAt,
            String deletedBy
    ) {
        this.id = id;
        this.empresaId = empresaId;
        this.username = username;
        this.email = email;
        this.rol = rol;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
        this.activo = activo;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(String empresaId) {
        this.empresaId = empresaId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Instant creadoEn) {
        this.creadoEn = creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(Instant actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioJpaEntity that = (UsuarioJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
