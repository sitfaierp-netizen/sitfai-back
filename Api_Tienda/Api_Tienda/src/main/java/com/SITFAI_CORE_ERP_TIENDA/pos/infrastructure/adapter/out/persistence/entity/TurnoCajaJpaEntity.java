package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad JPA para el Agregado TurnoCaja.
 * <p>
 * Regla AUD-01: Hereda de {@link AuditableJpaEntity} para trazabilidad automática (creado_en, creado_por, etc.).
 * Regla MT-01: Partición estricta mediante {@code empresa_id}.
 * Regla MONEY-01: Precisión contable {@code DECIMAL(19,4)}.
 */
@Entity(name = "PosTurnoCajaJpaEntity")
@Table(name = "pos_turno_caja")
public class TurnoCajaJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(name = "empresa_id", length = 36, nullable = false)
    private String empresaId;

    @Column(name = "caja_id", length = 36, nullable = false)
    private String cajaId;

    @Column(name = "cajero_id", length = 36)
    private String cajeroId;

    @Column(name = "sucursal_id", length = 36)
    private String sucursalId;

    @Column(name = "usuario_id", length = 36)
    private String usuarioId;

    @Column(name = "estado", length = 50, nullable = false)
    private String estado;

    @Column(name = "monto_apertura", precision = 19, scale = 4, nullable = false)
    private BigDecimal montoApertura;

    @Column(name = "monto_cierre", precision = 19, scale = 4)
    private BigDecimal montoCierre;

    @Column(name = "total_teorico", precision = 19, scale = 4)
    private BigDecimal totalTeorico;

    @Column(name = "monto_esperado", precision = 19, scale = 4)
    private BigDecimal montoEsperado;

    @Column(name = "monto_declarado", precision = 19, scale = 4)
    private BigDecimal montoDeclarado;

    @Column(name = "descuadre", precision = 19, scale = 4)
    private BigDecimal descuadre;

    @Column(name = "diferencia", precision = 19, scale = 4)
    private BigDecimal diferencia;

    @Column(name = "fecha_apertura")
    private Instant fechaApertura;

    @Column(name = "fecha_cierre")
    private Instant fechaCierre;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @OneToMany(mappedBy = "turno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransaccionCajaJpaEntity> transacciones = new ArrayList<>();

    public TurnoCajaJpaEntity() {
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

    public String getCajaId() {
        return cajaId;
    }

    public void setCajaId(String cajaId) {
        this.cajaId = cajaId;
    }

    public String getCajeroId() {
        return cajeroId;
    }

    public void setCajeroId(String cajeroId) {
        this.cajeroId = cajeroId;
    }

    public String getSucursalId() {
        return sucursalId;
    }

    public void setSucursalId(String sucursalId) {
        this.sucursalId = sucursalId;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public BigDecimal getMontoApertura() {
        return montoApertura;
    }

    public void setMontoApertura(BigDecimal montoApertura) {
        this.montoApertura = montoApertura;
    }

    public BigDecimal getMontoCierre() {
        return montoCierre;
    }

    public void setMontoCierre(BigDecimal montoCierre) {
        this.montoCierre = montoCierre;
    }

    public BigDecimal getTotalTeorico() {
        return totalTeorico;
    }

    public void setTotalTeorico(BigDecimal totalTeorico) {
        this.totalTeorico = totalTeorico;
    }

    public BigDecimal getMontoEsperado() {
        return montoEsperado;
    }

    public void setMontoEsperado(BigDecimal montoEsperado) {
        this.montoEsperado = montoEsperado;
    }

    public BigDecimal getMontoDeclarado() {
        return montoDeclarado;
    }

    public void setMontoDeclarado(BigDecimal montoDeclarado) {
        this.montoDeclarado = montoDeclarado;
    }

    public BigDecimal getDescuadre() {
        return descuadre;
    }

    public void setDescuadre(BigDecimal descuadre) {
        this.descuadre = descuadre;
    }

    public BigDecimal getDiferencia() {
        return diferencia;
    }

    public void setDiferencia(BigDecimal diferencia) {
        this.diferencia = diferencia;
    }

    public Instant getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(Instant fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public Instant getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(Instant fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public List<TransaccionCajaJpaEntity> getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(List<TransaccionCajaJpaEntity> transacciones) {
        this.transacciones = transacciones;
    }

    public void agregarTransaccion(TransaccionCajaJpaEntity tx) {
        this.transacciones.add(tx);
        tx.setTurno(this);
    }
}
