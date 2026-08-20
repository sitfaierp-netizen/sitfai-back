package com.SITFAI_CORE_ERP_TIENDA.core_empresa.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.Instant;

/**
 * Entidad JPA para la persistencia de Sucursal en la tabla `core_sucursal`.
 */
@Entity
@Table(
        name = "core_sucursal",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_core_sucursal_empresa_codigo", columnNames = {"empresa_id", "codigo"})
        }
)
@SQLRestriction("estado != 'ELIMINADO'")
public class SucursalJpaEntity {

    @Id
    @Column(name = "id", length = 36, nullable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private EmpresaJpaEntity empresa;

    @Column(name = "codigo", length = 20, nullable = false)
    private String codigo;

    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;

    @Column(name = "estado", length = 30, nullable = false)
    private String estado;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private Instant actualizadoEn;

    public SucursalJpaEntity() {
    }

    public SucursalJpaEntity(
            String id,
            EmpresaJpaEntity empresa,
            String codigo,
            String nombre,
            String estado,
            Instant creadoEn,
            Instant actualizadoEn
    ) {
        this.id = id;
        this.empresa = empresa;
        this.codigo = codigo;
        this.nombre = nombre;
        this.estado = estado;
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EmpresaJpaEntity getEmpresa() {
        return empresa;
    }

    public void setEmpresa(EmpresaJpaEntity empresa) {
        this.empresa = empresa;
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

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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
}
