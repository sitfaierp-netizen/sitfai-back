package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.CascadeType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad JPA: Representa la tabla 'inventory_bodega'.
 * <p>
 * Pertenece exclusivamente a la Capa de Infraestructura (REGLA-1, REGLA-6).
 * No contiene lógica de negocio — solo mapeo ORM para persistencia relacional.
 * <p>
 * Reglas validadas:
 * <ul>
 *   <li>REGLA-6: Entidades JPA viven únicamente en infrastructure/adapter/out/persistence.</li>
 *   <li>MT-01 / MT-03: Columna {@code empresa_id} para aislamiento multitenant.</li>
 *   <li>BOD-01: Columna {@code sucursal_id}.</li>
 * </ul>
 */
@Entity
@Table(name = "inventory_bodega")
public class BodegaJpaEntity {

    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private UUID id;

    @Column(name = "empresa_id", nullable = false, updatable = false, length = 36)
    private UUID empresaId;

    @Column(name = "sucursal_id", nullable = false, length = 36)
    private UUID sucursalId;

    @Column(name = "codigo", nullable = false, length = 50)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "activa", nullable = false)
    private boolean activa;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    @OneToMany(
            mappedBy = "bodega",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<StockLoteJpaEntity> lotes = new ArrayList<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(
            name = "inventory_bodega_punto_reorden",
            joinColumns = @JoinColumn(name = "bodega_id")
    )
    @MapKeyColumn(name = "producto_id", length = 36)
    @Column(name = "punto_reorden", nullable = false, precision = 19, scale = 4)
    private Map<UUID, BigDecimal> puntosReorden = new HashMap<>();

    @OneToMany(
            mappedBy = "bodega",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<MovimientoInventarioJpaEntity> movimientos = new ArrayList<>();

    public BodegaJpaEntity() {
        // Constructor vacío requerido por JPA
    }

    public BodegaJpaEntity(
            UUID id,
            UUID empresaId,
            UUID sucursalId,
            String codigo,
            String nombre,
            boolean activa,
            String tipo,
            Instant creadoEn,
            Instant actualizadoEn,
            List<StockLoteJpaEntity> lotes,
            Map<UUID, BigDecimal> puntosReorden,
            List<MovimientoInventarioJpaEntity> movimientos) {
        this.id = id;
        this.empresaId = empresaId;
        this.sucursalId = sucursalId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.activa = activa;
        this.tipo = tipo;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
        this.lotes = (lotes != null) ? lotes : new ArrayList<>();
        this.puntosReorden = (puntosReorden != null) ? puntosReorden : new HashMap<>();
        this.movimientos = (movimientos != null) ? movimientos : new ArrayList<>();
    }

    public void agregarMovimiento(MovimientoInventarioJpaEntity movimiento) {
        this.movimientos.add(movimiento);
        movimiento.setBodega(this);
    }

    public void agregarLote(StockLoteJpaEntity lote) {
        this.lotes.add(lote);
        lote.setBodega(this);
    }

    // Getters y Setters

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

    public UUID getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(UUID sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isActiva() {
        return activa;
    }

    public void setActiva(boolean activa) {
        this.activa = activa;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
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

    public List<StockLoteJpaEntity> getLotes() {
        return lotes;
    }

    public void setLotes(List<StockLoteJpaEntity> lotes) {
        this.lotes = lotes;
    }

    public Map<UUID, BigDecimal> getPuntosReorden() {
        return puntosReorden;
    }

    public void setPuntosReorden(Map<UUID, BigDecimal> puntosReorden) {
        this.puntosReorden = puntosReorden;
    }

    public List<MovimientoInventarioJpaEntity> getMovimientos() {
        return movimientos;
    }

    public void setMovimientos(List<MovimientoInventarioJpaEntity> movimientos) {
        this.movimientos = movimientos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BodegaJpaEntity that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
