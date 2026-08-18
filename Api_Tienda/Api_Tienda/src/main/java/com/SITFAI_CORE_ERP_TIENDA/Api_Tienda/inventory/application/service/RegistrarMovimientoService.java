package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.MovimientoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarMovimientoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.mapper.InventarioApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.MovimientoInventario;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.TipoMovimiento;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.RegistrarMovimientoUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * APPLICATION SERVICE: Registrar Movimiento de Inventario
 * ═══════════════════════════════════════════════════════════════════════════════
 * <p>
 * Orquesta el caso de uso de registrar una ENTRADA o SALIDA de stock en una Bodega.
 * <p>
 * ── FLUJO ────────────────────────────────────────────────────────────────────
 * 1. Construye los Value Objects desde el Command.
 * 2. Busca la Bodega via repositorio con el {@code empresaId} como filtro de tenant (MT-01, MT-02).
 * 3. Llama a {@code bodega.registrarMovimiento(...)} — el Dominio aplica la invariante BOD-05.
 *    Si el stock fuera negativo, el Dominio lanza {@code StockInsuficienteException}
 *    y esta capa NO la captura — fluye hasta el adaptador REST.
 * 4. Persiste el Agregado actualizado (con el nuevo movimiento en su lista interna).
 * 5. Drena y publica los Domain Events acumulados.
 * 6. Retorna el {@code MovimientoResponse} con el stock resultante.
 * <p>
 * ── INVARIANTE BOD-05 ────────────────────────────────────────────────────────
 * La Application Layer NO maneja ni captura {@code StockInsuficienteException}.
 * La excepción fluye libre hacia el adaptador REST para ser traducida a RFC 7807.
 * <p>
 * Reglas validadas: REGLA-1, BOD-03, BOD-04, BOD-05, MT-01, MT-02, AUD-03.
 */
@Service
public class RegistrarMovimientoService implements RegistrarMovimientoUseCase {

    private final BodegaRepository bodegaRepository;

    public RegistrarMovimientoService(BodegaRepository bodegaRepository) {
        this.bodegaRepository = bodegaRepository;
    }

    /**
     * {@inheritDoc}
     * <p>
     * ⚠️ Si {@code tipo == "SALIDA"} y el stock resultante sería negativo,
     * el Dominio lanza {@code StockInsuficienteException} — esta capa no la captura.
     *
     * @throws com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.StockInsuficienteException
     *         Propagada desde el Dominio cuando se viola BOD-05.
     * @throws IllegalArgumentException Si la Bodega no existe para el tenant dado, o si {@code tipo} es inválido.
     */
    @Override
    @Transactional
    public MovimientoResponse ejecutar(RegistrarMovimientoCommand command) {

        // ── 1. Construir Value Objects ─────────────────────────────────────────
        EmpresaId empresaId        = EmpresaId.de(command.empresaId());
        BodegaId bodegaId          = BodegaId.de(command.bodegaId());
        ProductoId productoId      = ProductoId.de(command.productoId());
        Cantidad cantidad           = Cantidad.de(command.cantidad());
        TipoMovimiento tipo         = parseTipoMovimiento(command.tipo());
        DocumentoFuenteId docFuente = new DocumentoFuenteId(
                command.docFuenteTipo(),
                command.docFuenteNumero()
        );

        // ── 2. Buscar el Agregado Bodega (con filtro de tenant — MT-01, MT-02) ─
        Bodega bodega = bodegaRepository
                .buscarPorId(bodegaId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Bodega '%s' no encontrada para la Empresa '%s'.",
                                bodegaId, empresaId)
                ));

        // ── 3. Delegar al Dominio — Invariante BOD-05 aplicada aquí ──────────
        //       Si hay StockInsuficienteException, FLUYE sin capturarse.
        bodega.registrarMovimiento(productoId, cantidad, tipo, docFuente);

        // ── 4. Persistir el Agregado actualizado ──────────────────────────────
        Bodega bodegaActualizada = bodegaRepository.guardar(bodega);

        // ── 5. Obtener el movimiento recién registrado y el stock resultante ──
        //       El último movimiento de la lista es el que acabamos de registrar.
        MovimientoInventario movimientoRegistrado = bodegaActualizada
                .getMovimientos()
                .getLast(); // Java 21+ SequencedCollection API

        BigDecimal stockResultante = bodegaActualizada.consultarStock(productoId);

        // ── 6. Drenar Domain Events ───────────────────────────────────────────
        bodegaActualizada.drainDomainEvents();
        // TODO: publicar eventos vía EventPublisher cuando se implemente (Infraestructura)

        // ── 7. Mapear y retornar respuesta ────────────────────────────────────
        return InventarioApplicationMapper.toMovimientoResponse(movimientoRegistrado, stockResultante);
    }

    /**
     * Parsea el String del tipo de movimiento al enum del Dominio.
     * Falla rápido si el valor no es válido.
     */
    private TipoMovimiento parseTipoMovimiento(String tipo) {
        try {
            return TipoMovimiento.valueOf(tipo.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    String.format("TipoMovimiento inválido: '%s'. Valores aceptados: ENTRADA, SALIDA.", tipo)
            );
        }
    }
}
