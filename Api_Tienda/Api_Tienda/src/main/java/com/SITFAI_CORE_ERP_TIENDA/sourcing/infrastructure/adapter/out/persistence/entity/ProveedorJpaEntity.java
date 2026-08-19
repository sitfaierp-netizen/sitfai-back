package com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;

/** JPA Entity: sourcing_proveedores. Vive SOLO en infraestructura. */
@Entity
@Table(name = "sourcing_proveedores",
       uniqueConstraints = @UniqueConstraint(name = "uq_sourcing_prov_empresa_ruc",
               columnNames = {"empresa_id", "ruc"}))
@SQLDelete(sql = "UPDATE sourcing_proveedores SET activo = false, deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("activo = true")
public class ProveedorJpaEntity {

    @Id @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "empresa_id", nullable = false, length = 36) private String empresaId;
    @Column(name = "ruc", nullable = false, length = 50) private String ruc;
    @Column(name = "razon_social", nullable = false, length = 255) private String razonSocial;
    @Column(name = "email_contacto", length = 100) private String emailContacto;
    @Column(name = "telefono", length = 50) private String telefono;
    @Column(name = "direccion", length = 500) private String direccion;
    @Column(name = "estado", nullable = false, length = 20) private String estado;
    @Column(name = "plazo_entrega_dias", nullable = false) private Integer plazoEntregaDias;
    @Column(name = "creado_en", nullable = false) private Instant creadoEn;
    @Column(name = "actualizado_en", nullable = false) private Instant actualizadoEn;

    @Column(name = "activo", nullable = false)
    private boolean activo = true;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_by", length = 36)
    private String deletedBy;

    protected ProveedorJpaEntity() {}

    public ProveedorJpaEntity(String id, String empresaId, String ruc, String razonSocial,
                              String emailContacto, String telefono, String direccion,
                              Integer plazoEntregaDias, String estado, Instant creadoEn,
                              Instant actualizadoEn, boolean activo, Instant deletedAt, String deletedBy) {
        this.id = id; this.empresaId = empresaId; this.ruc = ruc; this.razonSocial = razonSocial;
        this.emailContacto = emailContacto; this.telefono = telefono; this.direccion = direccion;
        this.plazoEntregaDias = plazoEntregaDias;
        this.estado = estado; this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn; this.activo = activo;
        this.deletedAt = deletedAt; this.deletedBy = deletedBy;
    }

    public String getId() { return id; }
    public String getEmpresaId() { return empresaId; }
    public String getRuc() { return ruc; }
    public String getRazonSocial() { return razonSocial; }
    public String getEmailContacto() { return emailContacto; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public Integer getPlazoEntregaDias() { return plazoEntregaDias; }
    public String getEstado() { return estado; }
    public Instant getCreadoEn() { return creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
    public void setActualizadoEn(Instant actualizadoEn) { this.actualizadoEn = actualizadoEn; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }
    public String getDeletedBy() { return deletedBy; }
    public void setDeletedBy(String deletedBy) { this.deletedBy = deletedBy; }
}
