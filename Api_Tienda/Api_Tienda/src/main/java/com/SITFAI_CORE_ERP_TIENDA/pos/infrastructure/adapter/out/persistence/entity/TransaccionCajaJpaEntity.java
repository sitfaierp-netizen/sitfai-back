package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pos_transaccion_caja")
public class TransaccionCajaJpaEntity {

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

    @Column(name = "fecha", nullable = false)
    private Instant fecha;

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

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }
}
