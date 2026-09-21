package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tienda_pedido")
public class PedidoJpaEntity extends AuditableJpaEntity {

    @Id
    private String id;

    @Column(name = "empresa_id", nullable = false)
    private String empresaId;

    @Column(name = "cliente_id", nullable = false)
    private String clienteId;

    @Column(name = "estado", nullable = false)
    private String estado;

    @Column(name = "total", nullable = false)
    private BigDecimal total;

    @Column(name = "moneda", nullable = false)
    private String moneda;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LineaPedidoJpaEntity> lineas = new ArrayList<>();

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmpresaId() { return empresaId; }
    public void setEmpresaId(String empresaId) { this.empresaId = empresaId; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public String getMoneda() { return moneda; }
    public void setMoneda(String moneda) { this.moneda = moneda; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public List<LineaPedidoJpaEntity> getLineas() { return lineas; }
    public void setLineas(List<LineaPedidoJpaEntity> lineas) { this.lineas = lineas; }
}
