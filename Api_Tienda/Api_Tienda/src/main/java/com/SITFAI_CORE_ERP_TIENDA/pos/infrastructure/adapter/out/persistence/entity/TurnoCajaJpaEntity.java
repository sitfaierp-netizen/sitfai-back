package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pos_turno_caja")
public class TurnoCajaJpaEntity {

    @Id
    @Column(length = 36, nullable = false)
    private String id;

    @Column(name = "empresa_id", length = 36, nullable = false)
    private String empresaId;

    @Column(name = "caja_id", length = 36, nullable = false)
    private String cajaId;

    @Column(name = "sucursal_id", length = 36, nullable = false)
    private String sucursalId;

    @Column(name = "usuario_id", length = 36, nullable = false)
    private String usuarioId;

    @Column(name = "estado", length = 50, nullable = false)
    private String estado;

    @Column(name = "monto_apertura", precision = 19, scale = 4, nullable = false)
    private BigDecimal montoApertura;

    @OneToMany(mappedBy = "turno", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TransaccionCajaJpaEntity> transacciones = new ArrayList<>();

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

    public List<TransaccionCajaJpaEntity> getTransacciones() {
        return transacciones;
    }

    public void setTransacciones(List<TransaccionCajaJpaEntity> transacciones) {
        this.transacciones = transacciones;
    }
}
