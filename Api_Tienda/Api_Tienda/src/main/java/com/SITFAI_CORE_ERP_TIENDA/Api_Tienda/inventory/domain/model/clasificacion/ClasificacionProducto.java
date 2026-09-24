package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.event.ProductoReclasificadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.AnalisisId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.CategoriaABC;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.MetricaMovimiento;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.ProductoId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root: ClasificacionProducto.
 * <p>
 * Representa la clasificación ABC vigente de un producto en una bodega específica.
 * Encapsula las métricas de movimiento del período y la categoría calculada por el
 * motor analítico WMS.
 * <p>
 * Invariantes implementadas:
 * <ul>
 *   <li>MT-01: EmpresaId obligatorio en toda instancia.</li>
 *   <li>REGLA-3: Los IDs son Value Objects, no primitivos.</li>
 *   <li>La categoría solo cambia a través del método {@link #reclasificar}.</li>
 *   <li>Un {@link ProductoReclasificadoEvent} se registra ÚNICAMENTE si la categoría cambia.</li>
 *   <li>El estado interno solo es modificable vía métodos del Agregado (encapsulamiento total).</li>
 * </ul>
 * <p>
 * Cero dependencias a Spring, Lombok o JPA (REGLA-1, ADR-001).
 */
public class ClasificacionProducto {

    // -------------------------------------------------------------------------
    // Identidad e invariantes de tenant
    // -------------------------------------------------------------------------
    private final AnalisisId  id;
    private final EmpresaId   empresaId;   // MT-01: discriminador raíz
    private final BodegaId    bodegaId;
    private final ProductoId  productoId;
    private final Instant     creadoEn;

    // -------------------------------------------------------------------------
    // Estado mutable controlado (solo accesible a través de comportamiento)
    // -------------------------------------------------------------------------
    private MetricaMovimiento metrica;
    private CategoriaABC      categoria;

    // -------------------------------------------------------------------------
    // Colección de Domain Events (REGLA-3)
    // -------------------------------------------------------------------------
    private final List<ProductoReclasificadoEvent> domainEvents;

    // -------------------------------------------------------------------------
    // Constructor privado — acceso exclusivo vía factory methods
    // -------------------------------------------------------------------------
    private ClasificacionProducto(
            AnalisisId  id,
            EmpresaId   empresaId,
            BodegaId    bodegaId,
            ProductoId  productoId,
            MetricaMovimiento metrica,
            CategoriaABC categoria,
            Instant creadoEn) {

        this.id          = Objects.requireNonNull(id,         "ClasificacionProducto: id es obligatorio.");
        this.empresaId   = Objects.requireNonNull(empresaId,  "ClasificacionProducto: empresaId es obligatorio (MT-01).");
        this.bodegaId    = Objects.requireNonNull(bodegaId,   "ClasificacionProducto: bodegaId es obligatorio.");
        this.productoId  = Objects.requireNonNull(productoId, "ClasificacionProducto: productoId es obligatorio.");
        this.metrica     = Objects.requireNonNull(metrica,    "ClasificacionProducto: metrica es obligatoria.");
        this.categoria   = Objects.requireNonNull(categoria,  "ClasificacionProducto: categoria es obligatoria.");
        this.creadoEn    = creadoEn != null ? creadoEn : Instant.now();
        this.domainEvents = new ArrayList<>();
    }

    // =========================================================================
    // FACTORY METHODS
    // =========================================================================

    /**
     * Crea un nuevo Análisis ABC para un producto, en estado {@link CategoriaABC#NO_CLASIFICADO}.
     * <p>
     * La métrica inicial es "sin movimientos" — se actualiza al ejecutar el primer análisis.
     *
     * @param empresaId  Tenant raíz (MT-01). Extraído del JWT — nunca del payload HTTP.
     * @param bodegaId   Bodega donde se analiza el producto.
     * @param productoId Producto bajo clasificación.
     * @return           Nueva instancia en estado NO_CLASIFICADO, lista para su primer análisis.
     */
    public static ClasificacionProducto iniciar(
            EmpresaId empresaId,
            BodegaId  bodegaId,
            ProductoId productoId) {
        return new ClasificacionProducto(
                AnalisisId.generar(),
                empresaId,
                bodegaId,
                productoId,
                MetricaMovimiento.sinMovimientos(),
                CategoriaABC.NO_CLASIFICADO,
                Instant.now()
        );
    }

    /**
     * Reconstitución desde persistencia — usado exclusivamente por el adaptador JPA.
     * No genera eventos de dominio.
     */
    public static ClasificacionProducto reconstituir(
            AnalisisId        id,
            EmpresaId         empresaId,
            BodegaId          bodegaId,
            ProductoId        productoId,
            MetricaMovimiento metrica,
            CategoriaABC      categoria,
            Instant           creadoEn) {
        return new ClasificacionProducto(id, empresaId, bodegaId, productoId, metrica, categoria, creadoEn);
    }

    // =========================================================================
    // COMPORTAMIENTO DE NEGOCIO PURO
    // =========================================================================

    /**
     * Reclasifica el producto aplicando la nueva métrica y categoría calculadas
     * por el motor analítico (Análisis ABC).
     * <p>
     * Invariante: Si la categoría cambia, se registra un {@link ProductoReclasificadoEvent}
     * en la colección interna del Agregado. Si la categoría NO cambia (mismo valor),
     * solo se actualiza la métrica — no se emite evento.
     * <p>
     * Este método es idempotente en cuanto a eventos: aplicar la misma categoría
     * con métricas diferentes actualiza los datos pero no genera ruido en el bus de eventos.
     *
     * @param nuevaMetrica  Métricas del período de análisis más reciente. No puede ser null.
     * @param nuevaCategoria Categoría calculada por el motor ABC. No puede ser null
     *                       ni {@link CategoriaABC#NO_CLASIFICADO} (la reclasificación siempre
     *                       produce una categoría activa).
     * @throws IllegalArgumentException si nuevaCategoria es null.
     */
    public void reclasificar(MetricaMovimiento nuevaMetrica, CategoriaABC nuevaCategoria) {
        Objects.requireNonNull(nuevaMetrica,   "reclasificar: la nuevaMetrica no puede ser null.");
        Objects.requireNonNull(nuevaCategoria, "reclasificar: la nuevaCategoria no puede ser null.");

        CategoriaABC categoriaAnterior = this.categoria;

        // Siempre actualizamos la métrica (refleja el período más reciente)
        this.metrica   = nuevaMetrica;
        this.categoria = nuevaCategoria;

        // Evento solo si la categoría efectivamente cambió (evitar ruido en el bus)
        if (categoriaAnterior != nuevaCategoria) {
            domainEvents.add(ProductoReclasificadoEvent.of(
                    this.empresaId,
                    this.bodegaId,
                    this.productoId,
                    this.id,
                    categoriaAnterior,
                    nuevaCategoria
            ));
        }
    }

    // =========================================================================
    // GESTIÓN DE DOMAIN EVENTS (REGLA-3)
    // =========================================================================

    /**
     * Retorna los eventos de dominio acumulados, listos para ser publicados por
     * el Application Service.
     */
    public List<ProductoReclasificadoEvent> pullDomainEvents() {
        List<ProductoReclasificadoEvent> eventos = Collections.unmodifiableList(
                new ArrayList<>(domainEvents));
        domainEvents.clear();
        return eventos;
    }

    /**
     * Retorna los eventos pendientes sin consumirlos (solo lectura — útil en tests).
     */
    public List<ProductoReclasificadoEvent> peekDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    // =========================================================================
    // GETTERS (SOLO LECTURA — sin setters)
    // =========================================================================

    public AnalisisId          getId()         { return id; }
    public EmpresaId           getEmpresaId()  { return empresaId; }
    public BodegaId            getBodegaId()   { return bodegaId; }
    public ProductoId          getProductoId() { return productoId; }
    public MetricaMovimiento   getMetrica()    { return metrica; }
    public CategoriaABC        getCategoria()  { return categoria; }
    public Instant             getCreadoEn()   { return creadoEn; }

    /** Indica si este producto ya tiene una clasificación activa. */
    public boolean estaClasificado() {
        return categoria.estaClasificado();
    }

    @Override
    public String toString() {
        return "ClasificacionProducto{id=" + id
                + ", empresa=" + empresaId
                + ", bodega=" + bodegaId
                + ", producto=" + productoId
                + ", categoria=" + categoria
                + ", metrica=" + metrica + "}";
    }
}
