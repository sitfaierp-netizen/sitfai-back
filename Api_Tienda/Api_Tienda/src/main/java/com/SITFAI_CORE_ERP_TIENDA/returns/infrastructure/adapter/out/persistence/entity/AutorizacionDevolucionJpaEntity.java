package com.SITFAI_CORE_ERP_TIENDA.returns.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.returns.domain.model.rma.vo.EstadoRma;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "returns_autorizacion_devolucion")
public class AutorizacionDevolucionJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "empresa_id", length = 36, nullable = false, updatable = false)
    private String empresaId;

    @Column(name = "documento_fuente_id", length = 36, nullable = false, updatable = false)
    private String documentoFuenteId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", length = 30, nullable = false)
    private EstadoRma estado;

    @OneToMany(mappedBy = "autorizacionDevolucion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaDevolucionJpaEntity> lineas = new ArrayList<>();

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }
    public String getDocumentoFuenteId() { return documentoFuenteId; }
    public void setDocumentoFuenteId(String documentoFuenteId) { this.documentoFuenteId = documentoFuenteId; }
    public EstadoRma getEstado() { return estado; }
    public void setEstado(EstadoRma estado) { this.estado = estado; }
    public List<LineaDevolucionJpaEntity> getLineas() { return lineas; }
    public void setLineas(List<LineaDevolucionJpaEntity> lineas) { this.lineas = lineas; }
}
