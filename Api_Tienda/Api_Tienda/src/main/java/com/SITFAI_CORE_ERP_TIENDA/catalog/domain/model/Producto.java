package com.SITFAI_CORE_ERP_TIENDA.catalog.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.event.ProductoCreadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.CategoriaId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.EstadoProducto;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.Impuesto;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.catalog.domain.valueobject.UnidadMedida;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root: Producto del Catálogo.
 *
 * Source of Truth de todos los productos de la plataforma SITFAI ERP.
 *
 * Invariantes implementadas (fail-fast en factory/mutadores):
 * - [INV-CAT-01] SKU no puede ser nulo ni vacío.
 * - [INV-CAT-02] Nombre no puede ser nulo ni vacío.
 * - [INV-CAT-03] precioVenta DEBE ser mayor que CERO.
 * - [INV-CAT-04] precioCompra no puede ser negativo (puede ser 0).
 * - [INV-CAT-05] Transición DESCONTINUADO → ACTIVO está prohibida.
 * - [MT-01]     empresaId es el discriminador de tenant — obligatorio.
 *
 * Eventos emitidos:
 * - ProductoCreadoEvent → al crear un nuevo Producto.
 *
 * Cero dependencias a Spring, JPA, Lombok, Jackson (Regla 1, ADR-001).
 */
public class Producto {

    // -------------------------------------------------------------------------
    // Estado inmutable de identidad
    // -------------------------------------------------------------------------
    private final ProductoId productoId;
    private final EmpresaId  empresaId;   // MT-01: discriminador de tenant
    private final String     sku;
    private final Instant    creadoEn;

    // -------------------------------------------------------------------------
    // Estado mutable — solo accesible a través de métodos de negocio
    // -------------------------------------------------------------------------
    private String       nombre;
    private String       descripcion;
    private CategoriaId  categoriaId;
    private UnidadMedida unidadMedida;
    private BigDecimal   precioCompra;
    private BigDecimal   precioVenta;
    private Impuesto     impuesto;
    private String       codigoBarras;    // nullable
    private EstadoProducto estado;
    private Instant      actualizadoEn;
    private boolean      activo = true;
    private Instant      deletedAt;
    private String       deletedBy;

    // -------------------------------------------------------------------------
    // Acumulador de Domain Events (drenado por el Application Service)
    // -------------------------------------------------------------------------
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Constructor privado — acceso exclusivo vía factory methods
    // -------------------------------------------------------------------------
    private Producto(ProductoId productoId, EmpresaId empresaId, String sku,
                     String nombre, String descripcion, CategoriaId categoriaId,
                     UnidadMedida unidadMedida, BigDecimal precioCompra,
                     BigDecimal precioVenta, Impuesto impuesto, String codigoBarras) {

        // Validaciones de identidad
        this.productoId = Objects.requireNonNull(productoId, "ProductoId no puede ser nulo.");
        this.empresaId  = Objects.requireNonNull(empresaId,  "EmpresaId no puede ser nulo (MT-01).");

        // [INV-CAT-01] SKU
        if (sku == null || sku.isBlank()) {
            throw new DomainException("[INV-CAT-01] El SKU del Producto no puede estar vacío.");
        }
        this.sku = sku.trim().toUpperCase();

        // [INV-CAT-02] Nombre
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("[INV-CAT-02] El nombre del Producto no puede estar vacío.");
        }
        this.nombre = nombre.trim();

        // [INV-CAT-04] precioCompra ≥ 0
        if (precioCompra != null && precioCompra.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(
                    "[INV-CAT-04] El precioCompra no puede ser negativo. Recibido: " + precioCompra);
        }

        // [INV-CAT-03] precioVenta > 0
        validarPrecioVenta(precioVenta);

        this.descripcion  = descripcion;
        this.categoriaId  = Objects.requireNonNull(categoriaId, "CategoriaId no puede ser nulo.");
        this.unidadMedida = Objects.requireNonNull(unidadMedida, "UnidadMedida no puede ser nula.");
        this.precioCompra = precioCompra;
        this.precioVenta  = precioVenta;
        this.impuesto     = Objects.requireNonNull(impuesto, "Impuesto no puede ser nulo.");
        this.codigoBarras = codigoBarras; // nullable
        this.estado       = EstadoProducto.ACTIVO;
        this.creadoEn     = Instant.now();
        this.actualizadoEn = this.creadoEn;
    }

    // -------------------------------------------------------------------------
    // Factory method: creación de nuevo Producto
    // -------------------------------------------------------------------------
    /**
     * Crea un nuevo Producto y acumula el {@link ProductoCreadoEvent}.
     *
     * @param productoId   Identidad generada externamente (SolicitudId pattern).
     * @param empresaId    Tenant del sistema (MT-01) — proviene del JWT, nunca del cliente.
     * @param sku          Stock Keeping Unit — único por empresa.
     * @param nombre       Nombre comercial del producto.
     * @param descripcion  Descripción detallada (nullable).
     * @param categoriaId  Categoría a la que pertenece.
     * @param unidadMedida Unidad de medida de venta.
     * @param precioCompra Costo de adquisición (≥ 0).
     * @param precioVenta  Precio de venta al público (> 0) — [INV-CAT-03].
     * @param impuesto     Tipo de impuesto DIAN aplicable.
     * @param codigoBarras Código de barras / EAN (nullable).
     * @return             Nueva instancia de Producto en estado ACTIVO con evento acumulado.
     */
    public static Producto crear(ProductoId productoId, EmpresaId empresaId,
                                  String sku, String nombre, String descripcion,
                                  CategoriaId categoriaId, UnidadMedida unidadMedida,
                                  BigDecimal precioCompra, BigDecimal precioVenta,
                                  Impuesto impuesto, String codigoBarras) {
        Producto p = new Producto(productoId, empresaId, sku, nombre, descripcion,
                categoriaId, unidadMedida, precioCompra, precioVenta, impuesto, codigoBarras);

        // Acumular evento de dominio para publicación posterior (AUD-03)
        p.domainEvents.add(ProductoCreadoEvent.of(
                productoId.valor(), empresaId.valor(), p.sku, p.nombre, p.precioVenta));

        return p;
    }

    // -------------------------------------------------------------------------
    // Factory method: reconstitución desde persistencia (sin emitir eventos)
    // -------------------------------------------------------------------------
    public static Producto reconstituir(ProductoId productoId, EmpresaId empresaId,
                                         String sku, String nombre, String descripcion,
                                         CategoriaId categoriaId, UnidadMedida unidadMedida,
                                         BigDecimal precioCompra, BigDecimal precioVenta,
                                         Impuesto impuesto, String codigoBarras,
                                         EstadoProducto estado, Instant creadoEn,
                                         Instant actualizadoEn, boolean activo,
                                         Instant deletedAt, String deletedBy) {
        Producto p = new Producto(productoId, empresaId, sku, nombre, descripcion,
                categoriaId, unidadMedida, precioCompra, precioVenta, impuesto, codigoBarras);
        p.estado        = estado;
        p.actualizadoEn = actualizadoEn;
        p.activo        = activo;
        p.deletedAt     = deletedAt;
        p.deletedBy     = deletedBy;
        return p;
    }

    // -------------------------------------------------------------------------
    // Comportamiento de negocio puro
    // -------------------------------------------------------------------------

    /**
     * Actualiza los datos comerciales mutables del Producto.
     * Valida las invariantes [INV-CAT-02], [INV-CAT-03] y [INV-CAT-04].
     */
    public void actualizar(String nombre, String descripcion, CategoriaId categoriaId,
                           UnidadMedida unidadMedida, BigDecimal precioCompra,
                           BigDecimal precioVenta, Impuesto impuesto, String codigoBarras) {
        if (nombre == null || nombre.isBlank()) {
            throw new DomainException("[INV-CAT-02] El nombre del Producto no puede estar vacío.");
        }
        if (precioCompra != null && precioCompra.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException("[INV-CAT-04] El precioCompra no puede ser negativo.");
        }
        validarPrecioVenta(precioVenta);

        this.nombre       = nombre.trim();
        this.descripcion  = descripcion;
        this.categoriaId  = Objects.requireNonNull(categoriaId, "CategoriaId no puede ser nulo.");
        this.unidadMedida = Objects.requireNonNull(unidadMedida, "UnidadMedida no puede ser nula.");
        this.precioCompra = precioCompra;
        this.precioVenta  = precioVenta;
        this.impuesto     = Objects.requireNonNull(impuesto, "Impuesto no puede ser nulo.");
        this.codigoBarras = codigoBarras;
        this.actualizadoEn = Instant.now();
    }

    /**
     * Cambia el estado del Producto.
     * [INV-CAT-05] Prohíbe la transición DESCONTINUADO → ACTIVO.
     *
     * @param nuevoEstado Estado destino.
     * @throws DomainException si la transición está prohibida.
     */
    public void cambiarEstado(EstadoProducto nuevoEstado) {
        if (this.estado == EstadoProducto.DESCONTINUADO
                && nuevoEstado == EstadoProducto.ACTIVO) {
            throw new DomainException(
                    "[INV-CAT-05] Un Producto DESCONTINUADO no puede volver a estado ACTIVO. " +
                    "Cree un nuevo Producto con un nuevo SKU.");
        }
        this.estado = Objects.requireNonNull(nuevoEstado, "EstadoProducto no puede ser nulo.");
        this.actualizadoEn = Instant.now();
    }

    /**
     * Da de baja lógicamente el Producto (Soft Delete).
     */
    public void darDeBaja(String actorId) {
        if (!this.activo) {
            return;
        }
        Objects.requireNonNull(actorId, "El actorId no puede ser null.");
        this.activo = false;
        this.deletedAt = Instant.now();
        this.deletedBy = actorId;
        this.actualizadoEn = Instant.now();
    }

    // -------------------------------------------------------------------------
    // Drenado de eventos — Application Service llama esto post-persistencia
    // -------------------------------------------------------------------------
    public List<DomainEvent> drenaEventos() {
        List<DomainEvent> eventos = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(eventos);
    }

    // -------------------------------------------------------------------------
    // Validación privada reutilizable
    // -------------------------------------------------------------------------
    private void validarPrecioVenta(BigDecimal precio) {
        if (precio == null || precio.compareTo(BigDecimal.ZERO) < 0) {
            throw new DomainException(
                    "[INV-CAT-03] El precioVenta no puede ser negativo. Recibido: " + precio);
        }
    }

    // -------------------------------------------------------------------------
    // Getters (lectura inmutable — sin setters públicos)
    // -------------------------------------------------------------------------
    public ProductoId   getProductoId()    { return productoId; }
    public EmpresaId    getEmpresaId()     { return empresaId; }
    public String       getSku()           { return sku; }
    public String       getNombre()        { return nombre; }
    public String       getDescripcion()   { return descripcion; }
    public CategoriaId  getCategoriaId()   { return categoriaId; }
    public UnidadMedida getUnidadMedida()  { return unidadMedida; }
    public BigDecimal   getPrecioCompra()  { return precioCompra; }
    public BigDecimal   getPrecioVenta()   { return precioVenta; }
    public Impuesto     getImpuesto()      { return impuesto; }
    public String       getCodigoBarras()  { return codigoBarras; }
    public EstadoProducto getEstado()      { return estado; }
    public Instant      getCreadoEn()      { return creadoEn; }
    public Instant      getActualizadoEn() { return actualizadoEn; }
    public boolean      isActivo()         { return activo; }
    public Instant      getDeletedAt()     { return deletedAt; }
    public String       getDeletedBy()     { return deletedBy; }
    public List<DomainEvent> getDomainEvents() { return Collections.unmodifiableList(domainEvents); }
}
