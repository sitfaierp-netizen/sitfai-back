package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.entity;

import com.SITFAI_CORE_ERP_TIENDA.core.audit.infrastructure.persistence.entity.AuditableJpaEntity;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entidad JPA para las transacciones procesadas dentro de un Turno de Caja.
 * <p>
 * Regla AUD-01: Hereda de {@link AuditableJpaEntity}.
 * Regla MT-01: Partición estricta mediante {@code empresa_id}.
 * Regla MONEY-01: Precisión contable {@code DECIMAL(19,4)}.
 */
@Entity(name = "PosTransaccionCajaJpaEntity")
@Table(name = "pos_transaccion_caja")
public class TransaccionCajaJpaEntity extends AuditableJpaEntity {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "turno_id", nullable = false)
    private TurnoCajaJpaEntity turno;

    @Column(name = "empresa_id", length = 36, nullable = false)
    private String empresaId;

    @Column(name = "tipo", length = 50, nullable = false)
    private String tipo;

    @Column(name = "monto", precision = 19, scale = 4, nullable = false)
    private BigDecimal monto;

    @Column(name = "referencia")
    private String referencia;

    @Column(name = "documento_fuente_id", length = 100)
    private String documentoFuenteId;

    @Column(name = "fecha")
    private Instant fecha;

    @Column(name = "fecha_hora")
    private Instant fechaHora;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    public TransaccionCajaJpaEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TurnoCajaJpaEntity getTurno() {
        return turno;
    }

    public void setTurno(TurnoCajaJpaEntity turno) {
        this.turno = turno;
    }

    public String getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(String empresaId) {
        this.empresaId = empresaId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public String getReferencia() {
        return referencia;
    }

    public void setReferencia(String referencia) {
        this.referencia = referencia;
    }

    public String getDocumentoFuenteId() {
        return documentoFuenteId;
    }

    public void setDocumentoFuenteId(String documentoFuenteId) {
        this.documentoFuenteId = documentoFuenteId;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(Instant fechaHora) {
        this.fechaHora = fechaHora;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
