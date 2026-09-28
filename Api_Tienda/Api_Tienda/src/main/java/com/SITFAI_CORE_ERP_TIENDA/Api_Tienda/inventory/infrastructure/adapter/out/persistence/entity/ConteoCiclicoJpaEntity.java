package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.EstadoConteo;
import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad JPA: Mapeo de la tabla 'inventory_conteo_ciclico' (Agregado Raíz).
 * <p>
 * Regla MT-01: Aislamiento estricto por {@code empresa_id}.
 * Regla AUD-01: Auditoría completa extendiendo de {@link AuditableJpaEntity}.
 * Gestión de agregados: Colección de detalles con {@code CascadeType.ALL} y {@code orphanRemoval = true}.
 */
@Entity
@Table(
        name = "inventory_conteo_ciclico",
        indexes = {
                @Index(name = "idx_inv_conteo_empresa", columnList = "empresa_id"),
                @Index(name = "idx_inv_conteo_empresa_bodega", columnList = "empresa_id, bodega_id"),
                @Index(name = "idx_inv_conteo_empresa_estado", columnList = "empresa_id, estado")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inv_conteo_empresa_id",
                        columnNames = {"empresa_id", "id"}
                )
        }
)
public class ConteoCiclicoJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "empresa_id", length = 36, nullable = false, updatable = false)
    private String empresaId;

    @Column(name = "bodega_id", length = 36, nullable = false, updatable = false)
    private String bodegaId;

    @Column(name = "fecha_programada", nullable = false)
    private LocalDate fechaProgramada;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 30, nullable = false)
    private EstadoConteo estado;

    @OneToMany(
            mappedBy = "conteo",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<DetalleConteoJpaEntity> detalles = new ArrayList<>();

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    protected ConteoCiclicoJpaEntity() {
    }

    public ConteoCiclicoJpaEntity(
            String id,
            String empresaId,
            String bodegaId,
            LocalDate fechaProgramada,
            EstadoConteo estado) {
        this.id = id;
        this.empresaId = empresaId;
        this.bodegaId = bodegaId;
        this.fechaProgramada = fechaProgramada;
        this.estado = estado;
    }

    public void addDetalle(DetalleConteoJpaEntity detalle) {
        this.detalles.add(detalle);
        detalle.setConteo(this);
    }

    public void clearDetalles() {
        this.detalles.clear();
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

    public String getBodegaId() {
        return bodegaId;
    }

    public void setBodegaId(String bodegaId) {
        this.bodegaId = bodegaId;
    }

    public LocalDate getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(LocalDate fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    public EstadoConteo getEstado() {
        return estado;
    }

    public void setEstado(EstadoConteo estado) {
        this.estado = estado;
    }

    public List<DetalleConteoJpaEntity> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleConteoJpaEntity> detalles) {
        this.detalles = detalles != null ? detalles : new ArrayList<>();
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ConteoCiclicoJpaEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
