package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.util.Objects;

/**
 * Entidad JPA: Mapeo de la tabla 'inventory_detalle_conteo'.
 * <p>
 * Regla MT-01: Aislamiento estricto por {@code empresa_id}.
 * Regla AUD-01: Auditoría completa extendiendo de {@link AuditableJpaEntity}.
 */
@Entity
@Table(
        name = "inventory_detalle_conteo",
        indexes = {
                @Index(name = "idx_inv_det_conteo_empresa", columnList = "empresa_id"),
                @Index(name = "idx_inv_det_conteo_padre", columnList = "empresa_id, conteo_id"),
                @Index(name = "idx_inv_det_conteo_prod", columnList = "empresa_id, producto_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inv_det_conteo_prod",
                        columnNames = {"empresa_id", "conteo_id", "producto_id"}
                )
        }
)
public class DetalleConteoJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "empresa_id", length = 36, nullable = false, updatable = false)
    private String empresaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conteo_id", nullable = false)
    private ConteoCiclicoJpaEntity conteo;

    @Column(name = "producto_id", length = 36, nullable = false)
    private String productoId;

    @Column(name = "cantidad_teorica", nullable = false)
    private int cantidadTeorica;

    @Column(name = "cantidad_fisica")
    private Integer cantidadFisica;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    protected DetalleConteoJpaEntity() {
    }

    public DetalleConteoJpaEntity(
            String id,
            String empresaId,
            ConteoCiclicoJpaEntity conteo,
            String productoId,
            int cantidadTeorica,
            Integer cantidadFisica) {
        this.id = id;
        this.empresaId = empresaId;
        this.conteo = conteo;
        this.productoId = productoId;
        this.cantidadTeorica = cantidadTeorica;
        this.cantidadFisica = cantidadFisica;
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

    public ConteoCiclicoJpaEntity getConteo() {
        return conteo;
    }

    public void setConteo(ConteoCiclicoJpaEntity conteo) {
        this.conteo = conteo;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public int getCantidadTeorica() {
        return cantidadTeorica;
    }

    public void setCantidadTeorica(int cantidadTeorica) {
        this.cantidadTeorica = cantidadTeorica;
    }

    public Integer getCantidadFisica() {
        return cantidadFisica;
    }

    public void setCantidadFisica(Integer cantidadFisica) {
        this.cantidadFisica = cantidadFisica;
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
        if (!(o instanceof DetalleConteoJpaEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
