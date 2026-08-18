package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ConsultarStockQuery;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.StockResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.mapper.InventarioApplicationMapper;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.ConsultarStockUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * APPLICATION SERVICE: Consultar Stock
 * ═══════════════════════════════════════════════════════════════════════════════
 * <p>
 * Caso de uso de solo lectura. Obtiene el stock actual de un Producto en una
 * Bodega específica para el tenant del usuario autenticado.
 * <p>
 * ── FLUJO ────────────────────────────────────────────────────────────────────
 * 1. Construye los Value Objects desde el Query.
 * 2. Busca la Bodega con filtro de tenant (MT-02: un tenant no ve datos de otro).
 * 3. Consulta el stock via {@code bodega.consultarStock(productoId)}.
 * 4. Mapea y retorna el {@code StockResponse}.
 * <p>
 * ── NOTA SOBRE TRANSACCIONALIDAD ─────────────────────────────────────────────
 * Se usa {@code @Transactional(readOnly = true)} para optimización:
 * - No genera snapshot de Hibernate (menor overhead).
 * - El JDBC driver puede enrutar a réplicas de lectura.
 * <p>
 * Reglas validadas: REGLA-1, BOD-05 (stock >= 0 por dominio), MT-01, MT-02.
 */
@Service


public class ConsultarStockService implements ConsultarStockUseCase {

    private final BodegaRepository bodegaRepository;


    public ConsultarStockService(BodegaRepository bodegaRepository) {
        this.bodegaRepository = bodegaRepository;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException Si la Bodega no existe o no pertenece al tenant.
     */
    @Override
    @Transactional(readOnly = true)
    public StockResponse ejecutar(ConsultarStockQuery query) {

        // ── 1. Construir Value Objects ─────────────────────────────────────────
        EmpresaId empresaId   = EmpresaId.de(query.empresaId());
        BodegaId bodegaId     = BodegaId.de(query.bodegaId());
        ProductoId productoId = ProductoId.de(query.productoId());

        // ── 2. Buscar Bodega con filtro de tenant (MT-02) ─────────────────────
        Bodega bodega = bodegaRepository
                .buscarPorId(bodegaId, empresaId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Bodega '%s' no encontrada para la Empresa '%s'.",
                                bodegaId, empresaId)
                ));

        // ── 3. Consultar stock vía el Agregado (el único autorizado — BOD-03) ─
        //       El resultado es siempre >= 0 por invariante BOD-05.
        return InventarioApplicationMapper.toStockResponse(bodega, productoId);
    }
}
