package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * JPA Entity: purchasing_solicitud.
 * Vive EXCLUSIVAMENTE en infraestructura — el Dominio no la conoce (Regla 6).
 */
@Entity
@Table(name = "purchasing_solicitud")
public class SolicitudJpaEntity {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "empresa_id", nullable = false, length = 36)
    private String empresaId;

    @Column(name = "bodega_id", nullable = false, length = 36)
    private String bodegaId;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LineaSolicitudJpaEntity> lineas = new ArrayList<>();

    // ---- Constructor sin args requerido por JPA ----
    protected SolicitudJpaEntity() {}

    public SolicitudJpaEntity(String id, String empresaId, String bodegaId,
                               String estado, Instant creadoEn) {
        this.id        = id;
        this.empresaId = empresaId;
        this.bodegaId  = bodegaId;
        this.estado    = estado;
        this.creadoEn  = creadoEn;
    }

    // Getters y setters mínimos
    public String getId()              { return id; }
    public String getEmpresaId()       { return empresaId; }
    public String getBodegaId()        { return bodegaId; }
    public String getEstado()          { return estado; }
    public void   setEstado(String e)  { this.estado = e; }
    public Instant getCreadoEn()       { return creadoEn; }
    public List<LineaSolicitudJpaEntity> getLineas() { return lineas; }
}
