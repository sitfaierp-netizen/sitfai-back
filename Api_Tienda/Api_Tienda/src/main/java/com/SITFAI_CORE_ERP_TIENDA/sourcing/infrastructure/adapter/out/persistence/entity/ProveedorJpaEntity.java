package com.SITFAI_CORE_ERP_TIENDA.sourcing.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** JPA Entity: sourcing_proveedores. Vive SOLO en infraestructura. */
@Entity
@Table(name = "sourcing_proveedores",
       uniqueConstraints = @UniqueConstraint(name = "uq_sourcing_prov_empresa_ruc",
               columnNames = {"empresa_id", "ruc"}))
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
    @Column(name = "creado_en", nullable = false) private Instant creadoEn;

    protected ProveedorJpaEntity() {}

    public ProveedorJpaEntity(String id, String empresaId, String ruc, String razonSocial,
                              String emailContacto, String telefono, String direccion,
                              String estado, Instant creadoEn) {
        this.id = id; this.empresaId = empresaId; this.ruc = ruc; this.razonSocial = razonSocial;
        this.emailContacto = emailContacto; this.telefono = telefono; this.direccion = direccion;
        this.estado = estado; this.creadoEn = creadoEn;
    }

    public String getId() { return id; }
    public String getEmpresaId() { return empresaId; }
    public String getRuc() { return ruc; }
    public String getRazonSocial() { return razonSocial; }
    public String getEmailContacto() { return emailContacto; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public String getEstado() { return estado; }
    public Instant getCreadoEn() { return creadoEn; }
}
