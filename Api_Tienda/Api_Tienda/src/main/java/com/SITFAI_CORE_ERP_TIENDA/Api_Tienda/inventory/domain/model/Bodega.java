package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.MovimientoRegistradoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.StockActualizadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.StockReservadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.PuntoReordenAlcanzadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.StockInsuficienteException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.LoteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.PuntoReorden;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.SucursalId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * AGGREGATE ROOT: Bodega
 * ═══════════════════════════════════════════════════════════════════════════════
 * <p>
 * La Bodega es la raíz del Agregado en el Bounded Context de Inventario.
 * Es el único punto de entrada para registrar movimientos de stock.
 * Todo acceso al stock de productos debe pasar por este Agregado.
 * <p>
 * ── REGLAS DE NEGOCIO APLICADAS ────────────────────────────────────────────────
 * <ul>
 *   <li>BOD-01: Una Bodega pertenece a exactamente una Sucursal ({@code sucursalId}).</li>
 *   <li>BOD-02: El código de Bodega es único dentro de la Sucursal (validado en Application).</li>
 *   <li>BOD-03: La Bodega es el único lugar donde se registran movimientos de stock.</li>
 *   <li>BOD-04: No existe movimiento sin {@code DocumentoFuenteId} — obligatorio.</li>
 *   <li>BOD-05: ⚠️ INVARIANTE CRÍTICA — El stock NUNCA puede ser negativo.</li>
 *   <li>BOD-06: Las transferencias generan dos movimientos independientes (en Bodegas distintas).</li>
 *   <li>BOD-08: El sistema emitirá una alerta si el stock cae a <= PuntoReorden.</li>
 *   <li>MT-01: El {@code empresaId} está presente para multitenancy.</li>
 *   <li>AUD-01: Los campos {@code creadoEn} y {@code actualizadoEn} están presentes.</li>
 *   <li>AUD-03: Los Domain Events se acumulan para ser publicados por la capa de Aplicación.</li>
 * </ul>
 * <p>
 * ── AISLAMIENTO ────────────────────────────────────────────────────────────────
 * ⚠️ PROHIBIDO: Cero imports de JPA, Spring o Jackson. Este es Java puro.
 * El mapeo a la BD es responsabilidad EXCLUSIVA de la capa de Infraestructura.
 */
public final class Bodega {

    // ── Identidad ─────────────────────────────────────────────────────────────

    /** Identificador único de la Bodega (UUID — REGLA-6). */
    private final BodegaId id;

    // ── Multitenancy (MT-01) ──────────────────────────────────────────────────

    /** Tenant al que pertenece la Bodega. Nunca cambia una vez creada. */
    private final EmpresaId empresaId;

    // ── Pertenencia jerárquica (BOD-01) ───────────────────────────────────────

    /** Sucursal a la que pertenece esta Bodega. Nunca cambia una vez creada. */
    private final SucursalId sucursalId;

    // ── Atributos descriptivos ────────────────────────────────────────────────

    /**
     * Código único de la Bodega dentro de la Sucursal (BOD-02).
     * Ej: "BDG-001", "PRINCIPAL", "REFRIGERADOS".
     */
    private final String codigo;

    /** Nombre descriptivo de la Bodega. */
    private String nombre;

    /** Indica si la Bodega está operativa. Una Bodega inactiva no acepta movimientos. */
    private boolean activa;

    /** Clasificación del propósito logístico de esta bodega. */
    private final TipoBodega tipo;

    // ── Stock ─────────────────────────────────────────────────────────────────

    /**
     * Stock por producto y lote.
     */
    private final List<StockLote> lotes;

    /**
     * Puntos de reorden por producto (BOD-08).
     * Si no está presente, se asume un punto de reorden por defecto de 0.
     */
    private final Map<ProductoId, PuntoReorden> puntosReorden;

    // ── Historial de movimientos ──────────────────────────────────────────────

    /**
     * Movimientos registrados en este agregado durante la sesión actual.
     * Se persisten en la capa de Infraestructura al llamar al repositorio.
     */
    private final List<MovimientoInventario> movimientos;

    // ── Domain Events (AUD-03) ────────────────────────────────────────────────

    /**
     * Eventos de dominio acumulados durante esta transacción.
     * La capa de Aplicación los drena y los publica DESPUÉS de persistir el Agregado.
     */
    private final List<DomainEvent> domainEvents;

    // ── Auditoría (AUD-01) ────────────────────────────────────────────────────

    private final Instant creadoEn;
    private Instant actualizadoEn;
    private Long version;
    private boolean activo = true;
    private Instant deletedAt;
    private String deletedBy;

    // ═════════════════════════════════════════════════════════════════════════
    // CONSTRUCTORES (privados — solo accesibles vía factory methods)
    // ═════════════════════════════════════════════════════════════════════════

    private Bodega(
            BodegaId id,
            EmpresaId empresaId,
            SucursalId sucursalId,
            String codigo,
            String nombre,
            boolean activa,
            TipoBodega tipo,
            List<StockLote> lotes,
            Map<ProductoId, PuntoReorden> puntosReorden,
            List<MovimientoInventario> movimientos,
            Instant creadoEn,
            Instant actualizadoEn,
            Long version) {

        this.id = id;
        this.empresaId = empresaId;
        this.sucursalId = sucursalId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.activa = activa;
        this.tipo = tipo;
        this.lotes = lotes != null ? new ArrayList<>(lotes) : new ArrayList<>();
        this.puntosReorden = (puntosReorden != null) ? new HashMap<>(puntosReorden) : new HashMap<>();
        this.movimientos = new ArrayList<>(movimientos);
        this.domainEvents = new ArrayList<>();
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
        this.version = version;
    }

    private Bodega(
            BodegaId id,
            EmpresaId empresaId,
            SucursalId sucursalId,
            String codigo,
            String nombre,
            boolean activa,
            TipoBodega tipo,
            List<StockLote> lotes,
            Map<ProductoId, PuntoReorden> puntosReorden,
            List<MovimientoInventario> movimientos,
            Instant creadoEn,
            Instant actualizadoEn,
            Long version,
            boolean activo,
            Instant deletedAt,
            String deletedBy) {

        this.id = id;
        this.empresaId = empresaId;
        this.sucursalId = sucursalId;
        this.codigo = codigo;
        this.nombre = nombre;
        this.activa = activa;
        this.tipo = tipo;
        this.lotes = lotes != null ? new ArrayList<>(lotes) : new ArrayList<>();
        this.puntosReorden = (puntosReorden != null) ? new HashMap<>(puntosReorden) : new HashMap<>();
        this.movimientos = new ArrayList<>(movimientos);
        this.domainEvents = new ArrayList<>();
        this.creadoEn = creadoEn;
        this.actualizadoEn = actualizadoEn;
        this.version = version;
        this.activo = activo;
        this.deletedAt = deletedAt;
        this.deletedBy = deletedBy;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // FACTORY METHODS
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Crea una nueva Bodega. Stock inicial vacío (todos los productos en 0).
     *
     * @param empresaId  Tenant al que pertenecerá (MT-01).
     * @param sucursalId Sucursal a la que pertenecerá (BOD-01).
     * @param codigo     Código único dentro de la Sucursal (BOD-02).
     * @param nombre     Nombre descriptivo.
     * @return           Nueva instancia del Agregado Bodega.
     */
    public static Bodega crear(
            EmpresaId empresaId,
            SucursalId sucursalId,
            String codigo,
            String nombre) {
        return crear(empresaId, sucursalId, codigo, nombre, TipoBodega.VENTA);
    }

    /**
     * Crea una nueva Bodega con un tipo específico. Stock inicial vacío.
     *
     * @param empresaId  Tenant al que pertenecerá (MT-01).
     * @param sucursalId Sucursal a la que pertenecerá (BOD-01).
     * @param codigo     Código único dentro de la Sucursal (BOD-02).
     * @param nombre     Nombre descriptivo.
     * @param tipo       El tipo o propósito de la bodega.
     * @return           Nueva instancia del Agregado Bodega.
     */
    public static Bodega crear(
            EmpresaId empresaId,
            SucursalId sucursalId,
            String codigo,
            String nombre,
            TipoBodega tipo) {

        validarCamposObligatorios(empresaId, sucursalId, codigo, nombre);
        Objects.requireNonNull(tipo, "Bodega: tipo de bodega es obligatorio.");

        Instant ahora = Instant.now();
        return new Bodega(
                BodegaId.nuevo(),
                empresaId,
                sucursalId,
                codigo.trim().toUpperCase(),
                nombre.trim(),
                true,
                tipo,
                new ArrayList<>(),
                new HashMap<>(),
                new ArrayList<>(),
                ahora,
                ahora,
                0L,
                true,
                null,
                null
        );
    }



    public static Bodega reconstituir(
            BodegaId id,
            EmpresaId empresaId,
            SucursalId sucursalId,
            String codigo,
            String nombre,
            boolean activa,
            TipoBodega tipo,
            Map<ProductoId, BigDecimal> stock,
            Map<ProductoId, PuntoReorden> puntosReorden,
            List<MovimientoInventario> movimientos,
            Instant creadoEn,
            Instant actualizadoEn,
            Long version,
            boolean activo,
            Instant deletedAt,
            String deletedBy) {
        List<StockLote> lotesMigrados = new ArrayList<>();
        if (stock != null) {
            stock.forEach((k, v) -> lotesMigrados.add(new StockLote(k, LoteId.de("LEGACY"), v, null)));
        }
        return reconstituir(id, empresaId, sucursalId, codigo, nombre, activa, tipo, lotesMigrados, puntosReorden, movimientos, creadoEn, actualizadoEn, version, activo, deletedAt, deletedBy);
    }

    public static Bodega reconstituir(
            BodegaId id,
            EmpresaId empresaId,
            SucursalId sucursalId,
            String codigo,
            String nombre,
            boolean activa,
            TipoBodega tipo,
            List<StockLote> lotes,
            Map<ProductoId, PuntoReorden> puntosReorden,
            List<MovimientoInventario> movimientos,
            Instant creadoEn,
            Instant actualizadoEn,
            Long version,
            boolean activo,
            Instant deletedAt,
            String deletedBy) {

        Objects.requireNonNull(id,           "Bodega.reconstituir: id es obligatorio.");
        validarCamposObligatorios(empresaId, sucursalId, codigo, nombre);
        Objects.requireNonNull(tipo,         "Bodega.reconstituir: tipo de bodega es obligatorio.");

        return new Bodega(id, empresaId, sucursalId, codigo, nombre, activa, tipo,
                lotes, puntosReorden, movimientos, creadoEn, actualizadoEn, version, activo, deletedAt, deletedBy);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // SOFT DELETE
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Aplica la baja lógica a la Bodega.
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

    // ═════════════════════════════════════════════════════════════════════════
    // COMPORTAMIENTO DE DOMINIO — MÉTODO PRINCIPAL
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Registra un ingreso de stock en la Bodega.
     */
    public void registrarIngreso(
            ProductoId productoId,
            Cantidad cantidad,
            LoteId loteId,
            Instant fechaCaducidad,
            DocumentoFuenteId documentoFuente) {

        if (!this.activo) {
            throw new IllegalStateException(String.format("La Bodega '%s' ha sido eliminada y no puede recibir movimientos.", this.id));
        }

        if (!this.activa) {
            throw new IllegalStateException(String.format("La Bodega '%s' está inactiva y no puede recibir movimientos.", this.id));
        }

        Objects.requireNonNull(productoId, "registrarIngreso: productoId es obligatorio.");
        Objects.requireNonNull(cantidad, "registrarIngreso: cantidad es obligatoria.");
        Objects.requireNonNull(documentoFuente, "registrarIngreso: documentoFuente es obligatorio (BOD-04).");

        StockLote loteExistente = null;
        if (loteId != null) {
            loteExistente = this.lotes.stream()
                    .filter(l -> l.getProductoId().equals(productoId) && Objects.equals(l.getLoteId(), loteId))
                    .findFirst()
                    .orElse(null);
        }

        if (loteExistente != null) {
            loteExistente.agregar(cantidad.valor());
        } else {
            LoteId finalLoteId = loteId != null ? loteId : LoteId.de("DEFAULT");
            this.lotes.add(new StockLote(productoId, finalLoteId, cantidad.valor(), fechaCaducidad));
        }

        this.actualizadoEn = Instant.now();

        MovimientoInventario movimiento = MovimientoInventario.crear(
                this.id, productoId, this.empresaId, cantidad, TipoMovimiento.ENTRADA, loteId, documentoFuente
        );
        this.movimientos.add(movimiento);

        this.domainEvents.add(MovimientoRegistradoEvent.of(
                this.id, productoId, this.empresaId, TipoMovimiento.ENTRADA, cantidad, documentoFuente
        ));

        this.domainEvents.add(StockActualizadoEvent.of(
                this.id, productoId, this.empresaId, consultarStock(productoId)
        ));
    }

    /**
     * Legacy registrarMovimiento (para compatibilidad).
     */
    public void registrarMovimiento(
            ProductoId productoId,
            Cantidad cantidad,
            TipoMovimiento tipo,
            DocumentoFuenteId documentoFuente) {
        if (tipo == TipoMovimiento.ENTRADA) {
            registrarIngreso(productoId, cantidad, null, null, documentoFuente);
        } else {
            descontarStock(productoId, cantidad, documentoFuente);
        }
    }

    /**
     * Reingresa stock a la bodega proveniente de una devolución de POS (Logística Inversa).
     * CAJ-08: Reversión de stock.
     */
    public void reingresarStock(
            ProductoId productoId,
            Cantidad cantidad,
            LoteId loteId,
            DocumentoFuenteId documentoFuente) {
        
        // El reingreso es semánticamente un ingreso de stock, reutilizamos la lógica de registrarIngreso.
        // Al reingresar de un lote conocido, la fecha de caducidad original se mantiene si el lote aún existe en la lista,
        // o se asume nula si el lote ya se agotó por completo y fue removido (aunque FEFO lo manejará enviándolo al final).
        registrarIngreso(productoId, cantidad, loteId, null, documentoFuente);
    }

    /**
     * Descuenta stock de la Bodega aplicando la lógica FEFO.
     */
    public void descontarStock(
            ProductoId productoId,
            Cantidad cantidad,
            DocumentoFuenteId documentoFuente) {

        if (!this.activa) {
            throw new IllegalStateException(String.format("La Bodega '%s' está inactiva y no puede recibir movimientos.", this.id));
        }

        Objects.requireNonNull(productoId, "descontarStock: productoId es obligatorio.");
        Objects.requireNonNull(cantidad, "descontarStock: cantidad es obligatoria.");
        Objects.requireNonNull(documentoFuente, "descontarStock: documentoFuente es obligatorio (BOD-04).");

        BigDecimal stockTotal = consultarStock(productoId);

        if (stockTotal.compareTo(cantidad.valor()) < 0) {
            throw new StockInsuficienteException(this.id, productoId, stockTotal, cantidad.valor());
        }

        // Lógica FEFO: Ordenar por fecha de caducidad ascendente (nulos al final)
        List<StockLote> lotesProducto = this.lotes.stream()
                .filter(l -> l.getProductoId().equals(productoId) && l.getCantidad().compareTo(BigDecimal.ZERO) > 0)
                .sorted((l1, l2) -> {
                    if (l1.getFechaCaducidad() == null && l2.getFechaCaducidad() == null) return 0;
                    if (l1.getFechaCaducidad() == null) return 1;
                    if (l2.getFechaCaducidad() == null) return -1;
                    return l1.getFechaCaducidad().compareTo(l2.getFechaCaducidad());
                })
                .toList();

        BigDecimal cantidadRestante = cantidad.valor();

        for (StockLote lote : lotesProducto) {
            if (cantidadRestante.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal disponibleEnLote = lote.getCantidad();
            BigDecimal aDescontar = disponibleEnLote.compareTo(cantidadRestante) >= 0 ? cantidadRestante : disponibleEnLote;
            
            lote.descontar(aDescontar);
            cantidadRestante = cantidadRestante.subtract(aDescontar);

            MovimientoInventario mov = MovimientoInventario.crear(
                    this.id, productoId, this.empresaId, Cantidad.de(aDescontar), TipoMovimiento.SALIDA, lote.getLoteId(), documentoFuente
            );
            this.movimientos.add(mov);
        }

        this.actualizadoEn = Instant.now();
        BigDecimal nuevoStock = consultarStock(productoId);

        this.domainEvents.add(MovimientoRegistradoEvent.of(
                this.id, productoId, this.empresaId, TipoMovimiento.SALIDA, cantidad, documentoFuente
        ));

        this.domainEvents.add(StockActualizadoEvent.of(
                this.id, productoId, this.empresaId, nuevoStock
        ));

        // Verificar BOD-08 (Punto de Reorden)
        BigDecimal umbral = this.puntosReorden.getOrDefault(productoId, PuntoReorden.porDefecto()).valor();
        if (stockTotal.compareTo(umbral) > 0 && nuevoStock.compareTo(umbral) <= 0) {
            this.domainEvents.add(PuntoReordenAlcanzadoEvent.of(
                    this.empresaId, this.id, productoId, nuevoStock
            ));
        }
    }

    /**
     * Reserva stock en la Bodega aplicando la lógica FEFO.
     * Mueve el stock de "disponible" a "reservado" sin crear un Movimiento de Salida todavía.
     */
    public void reservarStock(
            ProductoId productoId,
            Cantidad cantidad,
            DocumentoFuenteId documentoFuente) {

        if (!this.activa) {
            throw new IllegalStateException(String.format("La Bodega '%s' está inactiva y no puede recibir reservas.", this.id));
        }

        Objects.requireNonNull(productoId, "reservarStock: productoId es obligatorio.");
        Objects.requireNonNull(cantidad, "reservarStock: cantidad es obligatoria.");
        Objects.requireNonNull(documentoFuente, "reservarStock: documentoFuente es obligatorio.");

        BigDecimal stockTotal = consultarStock(productoId);

        if (stockTotal.compareTo(cantidad.valor()) < 0) {
            throw new StockInsuficienteException(this.id, productoId, stockTotal, cantidad.valor());
        }

        // Lógica FEFO: Ordenar por fecha de caducidad ascendente (nulos al final)
        List<StockLote> lotesProducto = this.lotes.stream()
                .filter(l -> l.getProductoId().equals(productoId) && l.getCantidad().compareTo(BigDecimal.ZERO) > 0)
                .sorted((l1, l2) -> {
                    if (l1.getFechaCaducidad() == null && l2.getFechaCaducidad() == null) return 0;
                    if (l1.getFechaCaducidad() == null) return 1;
                    if (l2.getFechaCaducidad() == null) return -1;
                    return l1.getFechaCaducidad().compareTo(l2.getFechaCaducidad());
                })
                .toList();

        BigDecimal cantidadRestante = cantidad.valor();

        for (StockLote lote : lotesProducto) {
            if (cantidadRestante.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal disponibleEnLote = lote.getCantidad();
            BigDecimal aReservar = disponibleEnLote.compareTo(cantidadRestante) >= 0 ? cantidadRestante : disponibleEnLote;
            
            lote.reservar(aReservar);
            cantidadRestante = cantidadRestante.subtract(aReservar);
        }

        this.actualizadoEn = Instant.now();

        this.domainEvents.add(StockReservadoEvent.of(
                this.id, productoId, this.empresaId, cantidad, documentoFuente
        ));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GESTIÓN DE AJUSTE DE INVENTARIO
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Procesa matemáticamente la diferencia de un ajuste físico de inventario.
     * Si la diferencia es positiva, registra un ingreso (Sobrante).
     * Si la diferencia es negativa, registra una salida del lote específico (Faltante, Merma, etc.).
     */
    public void aplicarAjuste(
            ProductoId productoId,
            BigDecimal diferencia,
            LoteId loteId,
            Instant fechaCaducidad,
            DocumentoFuenteId documentoFuente) {

        Objects.requireNonNull(productoId, "aplicarAjuste: productoId es obligatorio.");
        Objects.requireNonNull(diferencia, "aplicarAjuste: diferencia es obligatoria.");
        Objects.requireNonNull(documentoFuente, "aplicarAjuste: documentoFuente es obligatorio.");

        if (diferencia.compareTo(BigDecimal.ZERO) == 0) {
            return; // Sin efecto
        }

        if (diferencia.compareTo(BigDecimal.ZERO) > 0) {
            // Sobrante de inventario -> registrar como ingreso
            registrarIngreso(productoId, Cantidad.de(diferencia), loteId, fechaCaducidad, documentoFuente);
        } else {
            // Faltante de inventario -> descontar cantidad absoluta
            BigDecimal cantidadADescontar = diferencia.abs();
            
            if (loteId != null) {
                // Descuento exacto de un lote (no usa FEFO automático)
                descontarStockDeLote(productoId, cantidadADescontar, loteId, documentoFuente);
            } else {
                // Faltante general sin lote especificado (usa FEFO)
                descontarStock(productoId, Cantidad.de(cantidadADescontar), documentoFuente);
            }
        }
    }

    private void descontarStockDeLote(ProductoId productoId, BigDecimal cantidad, LoteId loteId, DocumentoFuenteId documentoFuente) {
        if (!this.activa) {
            throw new IllegalStateException(String.format("La Bodega '%s' está inactiva.", this.id));
        }

        StockLote lote = this.lotes.stream()
                .filter(l -> l.getProductoId().equals(productoId) && Objects.equals(l.getLoteId(), loteId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No se encontró el Lote " + loteId + " para el Producto " + productoId));

        if (lote.getCantidad().compareTo(cantidad) < 0) {
            throw new StockInsuficienteException(this.id, productoId, lote.getCantidad(), cantidad);
        }

        lote.descontar(cantidad);
        
        MovimientoInventario mov = MovimientoInventario.crear(
                this.id, productoId, this.empresaId, Cantidad.de(cantidad), TipoMovimiento.SALIDA, loteId, documentoFuente
        );
        this.movimientos.add(mov);
        
        this.actualizadoEn = Instant.now();
        BigDecimal nuevoStock = consultarStock(productoId);

        this.domainEvents.add(MovimientoRegistradoEvent.of(
                this.id, productoId, this.empresaId, TipoMovimiento.SALIDA, Cantidad.de(cantidad), documentoFuente
        ));
        this.domainEvents.add(StockActualizadoEvent.of(
                this.id, productoId, this.empresaId, nuevoStock
        ));
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GESTIÓN DE PUNTO DE REORDEN
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Establece el punto de reorden para un producto específico.
     *
     * @param productoId   Identificador del producto.
     * @param puntoReorden Valor del punto de reorden (>= 0).
     */
    public void establecerPuntoReorden(ProductoId productoId, PuntoReorden puntoReorden) {
        Objects.requireNonNull(productoId, "establecerPuntoReorden: productoId es obligatorio.");
        Objects.requireNonNull(puntoReorden, "establecerPuntoReorden: puntoReorden es obligatorio.");
        this.puntosReorden.put(productoId, puntoReorden);
        this.actualizadoEn = Instant.now();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // CONSULTAS DE STOCK Y ESTADO
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Determina si esta bodega permite registrar ventas comerciales.
     * Solo las bodegas de tipo VENTA y que se encuentran activas lo permiten.
     *
     * @return true si es apta para venta, false si es cuarentena, merma o está inactiva.
     */
    public boolean puedeVender() {
        return this.tipo == TipoBodega.VENTA && this.activa;
    }

    /**
     * Retorna el stock actual de un producto en esta Bodega.
     * Un producto sin movimientos retorna {@code BigDecimal.ZERO}.
     *
     * @param productoId Producto a consultar.
     * @return Stock actual (siempre >= 0 por invariante BOD-05).
     */
    public BigDecimal consultarStock(ProductoId productoId) {
        Objects.requireNonNull(productoId, "consultarStock: productoId es obligatorio.");
        return this.lotes.stream()
                .filter(l -> l.getProductoId().equals(productoId))
                .map(StockLote::getCantidad)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Retorna el stock reservado actual de un producto en esta Bodega.
     * Un producto sin movimientos retorna {@code BigDecimal.ZERO}.
     *
     * @param productoId Producto a consultar.
     * @return Stock reservado actual.
     */
    public BigDecimal consultarStockReservado(ProductoId productoId) {
        Objects.requireNonNull(productoId, "consultarStockReservado: productoId es obligatorio.");
        return this.lotes.stream()
                .filter(l -> l.getProductoId().equals(productoId))
                .map(StockLote::getCantidadReservada)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GESTIÓN DE DOMAIN EVENTS (Patrón "Pull" para la capa de Aplicación)
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Retorna los Domain Events acumulados durante esta transacción.
     * Vista inmutable — la capa de Aplicación usa {@code drainDomainEvents()} para publicarlos.
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Drena y limpia la lista de Domain Events.
     * La capa de Aplicación llama este método DESPUÉS de persistir el Agregado
     * y ANTES de publicar los eventos (AUD-03).
     */
    public List<DomainEvent> drainDomainEvents() {
        List<DomainEvent> eventos = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return Collections.unmodifiableList(eventos);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ACCESSORS (sin setters — el estado solo cambia vía métodos de dominio)
    // ═════════════════════════════════════════════════════════════════════════

    public BodegaId getId()                          { return id; }
    public EmpresaId getEmpresaId()                  { return empresaId; }
    public SucursalId getSucursalId()                { return sucursalId; }
    public String getCodigo()                        { return codigo; }
    public String getNombre()                        { return nombre; }
    public boolean isActiva()                        { return activa; }
    public TipoBodega getTipo()                      { return tipo; }
    public Instant getCreadoEn()                     { return creadoEn; }
    public Instant getActualizadoEn()                { return actualizadoEn; }
    public Long getVersion()                         { return version; }

    public boolean isActivo() {
        return activo;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    /** Vista inmutable del stock actual (Legacy compatibility) */
    public Map<ProductoId, BigDecimal> getStock() {
        Map<ProductoId, BigDecimal> stockMap = new HashMap<>();
        for (StockLote lote : lotes) {
            stockMap.merge(lote.getProductoId(), lote.getCantidad(), BigDecimal::add);
        }
        return Collections.unmodifiableMap(stockMap);
    }

    /** Vista inmutable de los lotes de stock. */
    public List<StockLote> getLotes() {
        return Collections.unmodifiableList(lotes);
    }

    /** Vista inmutable de los puntos de reorden. */
    public Map<ProductoId, PuntoReorden> getPuntosReorden() {
        return Collections.unmodifiableMap(puntosReorden);
    }

    /** Vista inmutable de los movimientos registrados en esta sesión. */
    public List<MovimientoInventario> getMovimientos() {
        return Collections.unmodifiableList(movimientos);
    }

    /**
     * Desactiva la Bodega. Una Bodega inactiva no puede registrar movimientos.
     * Solo roles autorizados pueden invocar este comportamiento (validado en Application).
     */
    public void desactivar() {
        this.activa = false;
        this.actualizadoEn = Instant.now();
    }

    /**
     * Reactiva una Bodega previamente desactivada.
     */
    public void activar() {
        this.activa = true;
        this.actualizadoEn = Instant.now();
    }

    // ═════════════════════════════════════════════════════════════════════════
    // VALIDACIÓN INTERNA
    // ═════════════════════════════════════════════════════════════════════════

    private static void validarCamposObligatorios(
            EmpresaId empresaId,
            SucursalId sucursalId,
            String codigo,
            String nombre) {

        Objects.requireNonNull(empresaId,  "Bodega: empresaId es obligatorio (MT-01).");
        Objects.requireNonNull(sucursalId, "Bodega: sucursalId es obligatorio (BOD-01).");
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Bodega: el código es obligatorio (BOD-02).");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Bodega: el nombre es obligatorio.");
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // EQUALS / HASHCODE — Por identidad (ID del Agregado)
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Bodega bodega)) return false;
        return Objects.equals(id, bodega.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Bodega{id=%s, empresa=%s, sucursal=%s, codigo='%s', activa=%s}",
                id, empresaId, sucursalId, codigo, activa);
    }
}
