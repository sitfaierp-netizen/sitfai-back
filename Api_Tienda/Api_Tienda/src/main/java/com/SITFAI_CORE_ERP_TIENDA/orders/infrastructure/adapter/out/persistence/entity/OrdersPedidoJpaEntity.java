package com.SITFAI_CORE_ERP_TIENDA.orders.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.core.document.domain.model.enums.DocumentStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity(name = "OrdersPedidoJpaEntity")
@Table(name = "orders_pedido")
public class OrdersPedidoJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "empresa_id", updatable = false, nullable = false, columnDefinition = "BINARY(16)")
    private UUID empresaId;

    @Column(name = "cliente_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID clienteId;

    @Column(name = "total_monetario", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalMonetario;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private DocumentStatus estado;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrdersLineaPedidoJpaEntity> lineas = new ArrayList<>();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getEmpresaId() { return empresaId; }
    public void setEmpresaId(UUID empresaId) { this.empresaId = empresaId; }

    public UUID getClienteId() { return clienteId; }
    public void setClienteId(UUID clienteId) { this.clienteId = clienteId; }

    public BigDecimal getTotalMonetario() { return totalMonetario; }
    public void setTotalMonetario(BigDecimal totalMonetario) { this.totalMonetario = totalMonetario; }

    public DocumentStatus getEstado() { return estado; }
    public void setEstado(DocumentStatus estado) { this.estado = estado; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public List<OrdersLineaPedidoJpaEntity> getLineas() { return lineas; }
    public void setLineas(List<OrdersLineaPedidoJpaEntity> lineas) { this.lineas = lineas; }
}
