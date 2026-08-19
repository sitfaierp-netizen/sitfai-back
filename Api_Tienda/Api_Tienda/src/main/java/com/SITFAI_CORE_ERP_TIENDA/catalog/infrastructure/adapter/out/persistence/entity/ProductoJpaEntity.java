package com.SITFAI_CORE_ERP_TIENDA.catalog.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

/** JPA Entity: catalog_productos. Vive SOLO en infraestructura. */
@Entity
@Table(name = "catalog_productos",
       uniqueConstraints = @UniqueConstraint(name = "uq_catalog_prod_empresa_sku",
               columnNames = {"empresa_id", "sku"}))
public class ProductoJpaEntity {

    @Id @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "empresa_id", nullable = false, length = 36) private String empresaId;
    @Column(name = "sku",        nullable = false, length = 100) private String sku;
    @Column(name = "nombre",     nullable = false, length = 255) private String nombre;
    @Column(name = "descripcion", length = 2000) private String descripcion;
    @Column(name = "categoria_id", nullable = false, length = 36) private String categoriaId;
    @Column(name = "unidad_medida", nullable = false, length = 20) private String unidadMedida;

    @Column(name = "precio_compra", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioCompra;

    @Column(name = "precio_venta", nullable = false, precision = 19, scale = 4)
    private BigDecimal precioVenta;

    @Column(name = "impuesto", nullable = false, length = 20) private String impuesto;
    @Column(name = "codigo_barras", length = 50) private String codigoBarras;
    @Column(name = "estado", nullable = false, length = 20) private String estado;
    @Column(name = "creado_en", nullable = false) private Instant creadoEn;
    @Column(name = "actualizado_en", nullable = false) private Instant actualizadoEn;

    protected ProductoJpaEntity() {}

    public ProductoJpaEntity(String id, String empresaId, String sku, String nombre,
                              String descripcion, String categoriaId, String unidadMedida,
                              BigDecimal precioCompra, BigDecimal precioVenta,
                              String impuesto, String codigoBarras, String estado,
                              Instant creadoEn, Instant actualizadoEn) {
        this.id = id; this.empresaId = empresaId; this.sku = sku; this.nombre = nombre;
        this.descripcion = descripcion; this.categoriaId = categoriaId;
        this.unidadMedida = unidadMedida; this.precioCompra = precioCompra;
        this.precioVenta = precioVenta; this.impuesto = impuesto;
        this.codigoBarras = codigoBarras; this.estado = estado;
        this.creadoEn = creadoEn; this.actualizadoEn = actualizadoEn;
    }

    // Getters
    public String getId()             { return id; }
    public String getEmpresaId()      { return empresaId; }
    public String getSku()            { return sku; }
    public String getNombre()         { return nombre; }
    public String getDescripcion()    { return descripcion; }
    public String getCategoriaId()    { return categoriaId; }
    public String getUnidadMedida()   { return unidadMedida; }
    public BigDecimal getPrecioCompra() { return precioCompra; }
    public BigDecimal getPrecioVenta()  { return precioVenta; }
    public String getImpuesto()       { return impuesto; }
    public String getCodigoBarras()   { return codigoBarras; }
    public String getEstado()         { return estado; }
    public void   setEstado(String e) { this.estado = e; }
    public void   setActualizadoEn(Instant i) { this.actualizadoEn = i; }
    public Instant getCreadoEn()      { return creadoEn; }
    public Instant getActualizadoEn() { return actualizadoEn; }
}
