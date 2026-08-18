package com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;

/** JPA Entity: catalog_categorias. Vive SOLO en infraestructura. */
@Entity
@Table(name = "catalog_categorias",
       uniqueConstraints = @UniqueConstraint(name = "uq_catalog_cat_empresa_nombre",
               columnNames = {"empresa_id", "nombre"}))
public class CategoriaJpaEntity {

    @Id @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "empresa_id",      nullable = false, length = 36)  private String empresaId;
    @Column(name = "nombre",          nullable = false, length = 255)  private String nombre;
    @Column(name = "categoria_padre_id", length = 36)                 private String categoriaPadreId;
    @Column(name = "estado",          nullable = false, length = 20)   private String estado;
    @Column(name = "creado_en",       nullable = false)                private Instant creadoEn;

    protected CategoriaJpaEntity() {}

    public CategoriaJpaEntity(String id, String empresaId, String nombre,
                               String categoriaPadreId, String estado, Instant creadoEn) {
        this.id = id; this.empresaId = empresaId; this.nombre = nombre;
        this.categoriaPadreId = categoriaPadreId; this.estado = estado; this.creadoEn = creadoEn;
    }

    public String getId()               { return id; }
    public String getEmpresaId()        { return empresaId; }
    public String getNombre()           { return nombre; }
    public void   setNombre(String n)   { this.nombre = n; }
    public String getCategoriaPadreId() { return categoriaPadreId; }
    public String getEstado()           { return estado; }
    public void   setEstado(String e)   { this.estado = e; }
    public Instant getCreadoEn()        { return creadoEn; }
}
