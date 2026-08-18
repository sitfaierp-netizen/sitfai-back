package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA: Representa la tabla 'tienda_pedido' (Aggregate Root en persistencia).
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura (REGLA-1, REGLA-6).
 * Almacena el estado relacional del pedido y sus líneas de detalle asociadas en cascada.
 */
@Entity
@Table(name = "tienda_pedido")
public class PedidoJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @Column(name = "empresa_id", nullable = false, updatable = false, length = 36)
    private UUID empresaId;

    @Column(name = "cliente_id", nullable = false, length = 36)
    private UUID clienteId;

    @Column(name = "estado", nullable = false, length = 30)
    private String estado;

    @Column(name = "total", nullable = false, precision = 19, scale = 4)
    private BigDecimal total;

    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<LineaPedidoJpaEntity> lineas = new ArrayList<>();

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    public PedidoJpaEntity() {
    }

    public PedidoJpaEntity(
            UUID id,
            UUID empresaId,
            UUID clienteId,
            String estado,
            BigDecimal total,
            String moneda,
            Instant creadoEn,
            Instant actualizadoEn) {
        this.id = id;
        this.empresaId = empresaId;
        this.clienteId = clienteId;
        this.estado = estado;
        this.total = total;
        this.moneda = moneda;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    public void agregarLinea(LineaPedidoJpaEntity linea) {
        lineas.add(linea);
        linea.setPedido(this);
    }

    public void limpiarLineas() {
        lineas.clear();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(UUID empresaId) {
        this.empresaId = empresaId;
    }

    public UUID getClienteId() {
        return clienteId;
    }

    public void setClienteId(UUID clienteId) {
        this.clienteId = clienteId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getMoneda() {
        return moneda;
    }

    public void setMoneda(String moneda) {
        this.moneda = moneda;
    }

    public List<LineaPedidoJpaEntity> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaPedidoJpaEntity> lineas) {
        this.lineas = lineas;
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(Instant creadoEn) {
        this.creadoEn = creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(Instant actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PedidoJpaEntity that = (PedidoJpaEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
