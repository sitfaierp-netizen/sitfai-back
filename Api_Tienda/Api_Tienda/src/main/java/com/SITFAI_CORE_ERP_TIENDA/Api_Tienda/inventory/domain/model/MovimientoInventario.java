package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidad de Dominio: Movimiento de Inventario.
 * <p>
 * Registra cada entrada o salida de stock de un producto en una Bodega específica.
 * Es inmutable una vez creada — los movimientos son registros históricos que no se
 * modifican (AUD-04: ningún registro financiero puede eliminarse físicamente).
 * <p>
 * Reglas de negocio aplicadas:
 * <ul>
 *   <li>BOD-03: Solo en Bodega se registran los movimientos de stock.</li>
 *   <li>BOD-04: Todo movimiento exige un {@code DocumentoFuenteId} — sin documento no hay movimiento.</li>
 *   <li>MT-01: El {@code empresaId} está presente para garantizar multitenancy.</li>
 *   <li>AUD-01: Registra {@code fechaRegistro} para auditoría.</li>
 * </ul>
 * <p>
 * ⚠️ AISLAMIENTO: Sin anotaciones JPA, Spring ni Jackson.
 * El mapeo a la capa de persistencia es responsabilidad exclusiva de la Infraestructura.
 */
public final class MovimientoInventario {

    /** Identificador único del movimiento (UUID, no auto-increment — REGLA-6). */
    private final UUID id;

    /** Bodega donde ocurre el movimiento (BOD-03). */
    private final BodegaId bodegaId;

    /** Producto cuyo stock se mueve. */
    private final ProductoId productoId;

    /** Tenant al que pertenece este movimiento (MT-01). */
    private final EmpresaId empresaId;

    /** Cantidad involucrada — siempre positiva; la dirección la define {@code tipo}. */
    private final Cantidad cantidad;

    /** Dirección del movimiento: ENTRADA o SALIDA. */
    private final TipoMovimiento tipo;

    /**
     * Documento que justifica el movimiento (BOD-04).
     * Nunca nulo — garantiza trazabilidad completa.
     */
    private final DocumentoFuenteId documentoFuente;

    /**
     * Marca de tiempo del registro del movimiento (AUD-01).
     * Se asigna en el momento de creación y es inmutable.
     */
    private final Instant fechaRegistro;

    /**
     * Constructor privado — la creación se realiza a través del factory method
     * para garantizar invariantes y coherencia.
     */
    private MovimientoInventario(
            UUID id,
            BodegaId bodegaId,
            ProductoId productoId,
            EmpresaId empresaId,
            Cantidad cantidad,
            TipoMovimiento tipo,
            DocumentoFuenteId documentoFuente,
            Instant fechaRegistro) {

        this.id = id;
        this.bodegaId = bodegaId;
        this.productoId = productoId;
        this.empresaId = empresaId;
        this.cantidad = cantidad;
        this.tipo = tipo;
        this.documentoFuente = documentoFuente;
        this.fechaRegistro = fechaRegistro;
    }

    /**
     * Factory method principal — crea un nuevo MovimientoInventario validando todos los campos.
     * <p>
     * El {@code Agregado Bodega} es el único que invoca este método — los servicios de
     * aplicación nunca instancian MovimientoInventario directamente.
     *
     * @param bodegaId         Bodega destino/origen del movimiento.
     * @param productoId       Producto afectado.
     * @param empresaId        Tenant del movimiento (BOD-01, MT-01).
     * @param cantidad         Cantidad de unidades (siempre positiva).
     * @param tipo             ENTRADA o SALIDA.
     * @param documentoFuente  Documento obligatorio que justifica el movimiento (BOD-04).
     * @return                 Nueva instancia inmutable de MovimientoInventario.
     */
    public static MovimientoInventario crear(
            BodegaId bodegaId,
            ProductoId productoId,
            EmpresaId empresaId,
            Cantidad cantidad,
            TipoMovimiento tipo,
            DocumentoFuenteId documentoFuente) {

        validarCampos(bodegaId, productoId, empresaId, cantidad, tipo, documentoFuente);

        return new MovimientoInventario(
                UUID.randomUUID(),
                bodegaId,
                productoId,
                empresaId,
                cantidad,
                tipo,
                documentoFuente,
                Instant.now()
        );
    }

    /**
     * Factory method de reconstitución — usado por la Infraestructura para reconstruir
     * la entidad desde la base de datos sin generar un nuevo UUID ni timestamp.
     */
    public static MovimientoInventario reconstituir(
            UUID id,
            BodegaId bodegaId,
            ProductoId productoId,
            EmpresaId empresaId,
            Cantidad cantidad,
            TipoMovimiento tipo,
            DocumentoFuenteId documentoFuente,
            Instant fechaRegistro) {

        validarCampos(bodegaId, productoId, empresaId, cantidad, tipo, documentoFuente);
        if (id == null) {
            throw new IllegalArgumentException("MovimientoInventario: el id no puede ser null al reconstituir.");
        }
        if (fechaRegistro == null) {
            throw new IllegalArgumentException("MovimientoInventario: la fechaRegistro no puede ser null al reconstituir.");
        }

        return new MovimientoInventario(id, bodegaId, productoId, empresaId, cantidad, tipo, documentoFuente, fechaRegistro);
    }

    // ── Validación interna ──────────────────────────────────────────────────────

    private static void validarCampos(
            BodegaId bodegaId,
            ProductoId productoId,
            EmpresaId empresaId,
            Cantidad cantidad,
            TipoMovimiento tipo,
            DocumentoFuenteId documentoFuente) {

        if (bodegaId == null)       throw new IllegalArgumentException("MovimientoInventario: bodegaId es obligatorio.");
        if (productoId == null)     throw new IllegalArgumentException("MovimientoInventario: productoId es obligatorio.");
        if (empresaId == null)      throw new IllegalArgumentException("MovimientoInventario: empresaId es obligatorio (MT-01).");
        if (cantidad == null)       throw new IllegalArgumentException("MovimientoInventario: cantidad es obligatoria.");
        if (tipo == null)           throw new IllegalArgumentException("MovimientoInventario: tipo es obligatorio.");
        if (documentoFuente == null)throw new IllegalArgumentException("MovimientoInventario: documentoFuente es obligatorio (BOD-04).");
    }

    // ── Accessors (sin setters — inmutable) ────────────────────────────────────

    public UUID getId()                           { return id; }
    public BodegaId getBodegaId()                 { return bodegaId; }
    public ProductoId getProductoId()             { return productoId; }
    public EmpresaId getEmpresaId()               { return empresaId; }
    public Cantidad getCantidad()                 { return cantidad; }
    public TipoMovimiento getTipo()               { return tipo; }
    public DocumentoFuenteId getDocumentoFuente() { return documentoFuente; }
    public Instant getFechaRegistro()             { return fechaRegistro; }

    @Override
    public String toString() {
        return String.format(
                "MovimientoInventario{id=%s, bodega=%s, producto=%s, tipo=%s, cantidad=%s, doc=%s, fecha=%s}",
                id, bodegaId, productoId, tipo, cantidad, documentoFuente, fechaRegistro
        );
    }
}
