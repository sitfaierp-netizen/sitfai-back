package com.SITFAI_CORE_ERP_TIENDA.replenishment.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.*;

/**
 * Entidad JPA: Representa la política de reposición automática de inventario.
 * <p>
 * Regla REGLA-6: JPA Entity vive EXCLUSIVAMENTE en infrastructure/adapter/out/persistence/.
 * Regla MT-01: Aislamiento estricto por {@code empresa_id}.
 * Regla AUD-01: Auditoría completa heredando de {@link AuditableJpaEntity}.
 */
@Entity
@Table(
        name = "replenishment_politica_inventario",
        indexes = {
                @Index(name = "idx_replenishment_pol_empresa", columnList = "empresa_id"),
                @Index(name = "idx_replenishment_pol_empresa_bodega", columnList = "empresa_id, bodega_id"),
                @Index(name = "idx_replenishment_pol_empresa_producto", columnList = "empresa_id, producto_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_replenishment_pol_tenant_bodega_producto",
                        columnNames = {"empresa_id", "bodega_id", "producto_id"}
                )
        }
)
public class PoliticaInventarioJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "empresa_id", length = 36, nullable = false, updatable = false)
    private String empresaId;

    @Column(name = "bodega_id", length = 36, nullable = false)
    private String bodegaId;

    @Column(name = "producto_id", length = 36, nullable = false)
    private String productoId;

    @Column(name = "punto_reorden", nullable = false)
    private int puntoReorden;

    @Column(name = "nivel_optimo", nullable = false)
    private int nivelOptimo;

    @Column(name = "activa", nullable = false)
    private boolean activa;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    protected PoliticaInventarioJpaEntity() {}

    public PoliticaInventarioJpaEntity(String id, String empresaId, String bodegaId,
                                       String productoId, int puntoReorden, int nivelOptimo,
                                       boolean activa) {
        this.id = id;
        this.empresaId = empresaId;
        this.bodegaId = bodegaId;
        this.productoId = productoId;
        this.puntoReorden = puntoReorden;
        this.nivelOptimo = nivelOptimo;
        this.activa = activa;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }

    public String getBodegaId() { return bodegaId; }
    public void setBodegaId(String bodegaId) { this.bodegaId = bodegaId; }

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }

    public int getPuntoReorden() { return puntoReorden; }
    public void setPuntoReorden(int puntoReorden) { this.puntoReorden = puntoReorden; }

    public int getNivelOptimo() { return nivelOptimo; }
    public void setNivelOptimo(int nivelOptimo) { this.nivelOptimo = nivelOptimo; }

    public boolean isActiva() { return activa; }
    public void setActiva(boolean activa) { this.activa = activa; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
