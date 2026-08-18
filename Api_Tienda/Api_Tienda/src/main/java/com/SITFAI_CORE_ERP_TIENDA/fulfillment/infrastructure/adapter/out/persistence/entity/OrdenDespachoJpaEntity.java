package com.SITFAI_CORE_ERP_TIENDA.fulfillment.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fulfillment_orden_despacho")
public class OrdenDespachoJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @Column(name = "empresa_id", length = 36, nullable = false)
    private String empresaId;

    @Column(name = "pedido_origen_id", length = 36, nullable = false)
    private String pedidoOrigenId;

    @Column(name = "estado", length = 50, nullable = false)
    private String estado;

    @Column(name = "direccion_local", nullable = false)
    private String direccionLocal;

    @Column(name = "ciudad", nullable = false)
    private String ciudad;

    @Column(name = "codigo_postal", nullable = false)
    private String codigoPostal;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "orden_despacho_id")
    private List<LineaDespachoJpaEntity> lineas = new ArrayList<>();

    public OrdenDespachoJpaEntity() {
    }

    public OrdenDespachoJpaEntity(String id, String empresaId, String pedidoOrigenId, String estado, String direccionLocal, String ciudad, String codigoPostal) {
        this.id = id;
        this.empresaId = empresaId;
        this.pedidoOrigenId = pedidoOrigenId;
        this.estado = estado;
        this.direccionLocal = direccionLocal;
        this.ciudad = ciudad;
        this.codigoPostal = codigoPostal;
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

    public String getPedidoOrigenId() {
        return pedidoOrigenId;
    }

    public void setPedidoOrigenId(String pedidoOrigenId) {
        this.pedidoOrigenId = pedidoOrigenId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDireccionLocal() {
        return direccionLocal;
    }

    public void setDireccionLocal(String direccionLocal) {
        this.direccionLocal = direccionLocal;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getCodigoPostal() {
        return codigoPostal;
    }

    public void setCodigoPostal(String codigoPostal) {
        this.codigoPostal = codigoPostal;
    }

    public List<LineaDespachoJpaEntity> getLineas() {
        return lineas;
    }

    public void setLineas(List<LineaDespachoJpaEntity> lineas) {
        this.lineas = lineas;
    }
}
