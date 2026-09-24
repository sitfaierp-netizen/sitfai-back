package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.CategoriaABC;
import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Entidad JPA: Representa la tabla 'inventory_clasificacion_producto'.
 * <p>
 * Regla REGLA-6: Entidades JPA viven EXCLUSIVAMENTE en infrastructure/adapter/out/persistence/.
 * Regla MT-01: Aislamiento estricto por {@code empresa_id}.
 * Regla MONEY-01: {@code valor_total_despachado} con precisión DECIMAL(19,4).
 * Regla AUD-01: Auditoría completa heredando de {@link AuditableJpaEntity}.
 */
@Entity
@Table(
        name = "inventory_clasificacion_producto",
        indexes = {
                @Index(name = "idx_inv_clasif_empresa", columnList = "empresa_id"),
                @Index(name = "idx_inv_clasif_empresa_bodega", columnList = "empresa_id, bodega_id"),
                @Index(name = "idx_inv_clasif_empresa_categoria", columnList = "empresa_id, categoria"),
                @Index(name = "idx_inv_clasif_empresa_producto", columnList = "empresa_id, producto_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_inv_clasif_empresa_bodega_prod",
                        columnNames = {"empresa_id", "bodega_id", "producto_id"}
                )
        }
)
public class ClasificacionProductoJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "empresa_id", length = 36, nullable = false, updatable = false)
    private String empresaId;

    @Column(name = "bodega_id", length = 36, nullable = false, updatable = false)
    private String bodegaId;

    @Column(name = "producto_id", length = 36, nullable = false, updatable = false)
    private String productoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", length = 20, nullable = false)
    private CategoriaABC categoria;

    @Column(name = "frecuencia_salida", nullable = false)
    private int frecuenciaSalida;

    @Column(name = "valor_total_despachado", nullable = false, precision = 19, scale = 4)
    private BigDecimal valorTotalDespachado;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    protected ClasificacionProductoJpaEntity() {
        // Constructor protegido para JPA
    }

    public ClasificacionProductoJpaEntity(
            String id,
            String empresaId,
            String bodegaId,
            String productoId,
            CategoriaABC categoria,
            int frecuenciaSalida,
            BigDecimal valorTotalDespachado) {
        this.id = id;
        this.empresaId = empresaId;
        this.bodegaId = bodegaId;
        this.productoId = productoId;
        this.categoria = categoria;
        this.frecuenciaSalida = frecuenciaSalida;
        this.valorTotalDespachado = valorTotalDespachado;
    }

    // Getters y Setters
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

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public CategoriaABC getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaABC categoria) {
        this.categoria = categoria;
    }

    public int getFrecuenciaSalida() {
        return frecuenciaSalida;
    }

    public void setFrecuenciaSalida(int frecuenciaSalida) {
        this.frecuenciaSalida = frecuenciaSalida;
    }

    public BigDecimal getValorTotalDespachado() {
        return valorTotalDespachado;
    }

    public void setValorTotalDespachado(BigDecimal valorTotalDespachado) {
        this.valorTotalDespachado = valorTotalDespachado;
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
        if (!(o instanceof ClasificacionProductoJpaEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
