package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.jpa.entity.ajuste;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.vo.MotivoAjuste;
import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.enums.DocumentStatus;
import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "inv_ajustes_inventario")
public class AjusteInventarioJpaEntity extends AuditableJpaEntity {

    @Id
    private UUID id;

    @Column(name = "empresa_id", nullable = false)
    private UUID empresaId;

    @Column(name = "bodega_id", nullable = false)
    private UUID bodegaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MotivoAjuste motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus estado;


    @Version
    private Long version;

    @OneToMany(mappedBy = "ajuste", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaAjusteJpaEntity> lineas = new ArrayList<>();

    public AjusteInventarioJpaEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }

    public UUID getBodegaId() { return bodegaId; }
    public void setBodegaId(UUID bodegaId) { this.bodegaId = bodegaId; }

    public MotivoAjuste getMotivo() { return motivo; }
    public void setMotivo(MotivoAjuste motivo) { this.motivo = motivo; }

    public DocumentStatus getEstado() { return estado; }
    public void setEstado(DocumentStatus estado) { this.estado = estado; }


    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public List<LineaAjusteJpaEntity> getLineas() { return lineas; }
    public void setLineas(List<LineaAjusteJpaEntity> lineas) {
        this.lineas.clear();
        if (lineas != null) {
            lineas.forEach(this::addLinea);
        }
    }

    public void addLinea(LineaAjusteJpaEntity linea) {
        lineas.add(linea);
        linea.setAjuste(this);
    }
}
