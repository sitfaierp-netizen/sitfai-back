package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.MovimientoResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarMovimientoCommand;

/**
 * Puerto de Entrada (Driving Port) — Caso de Uso: Registrar Movimiento de Inventario.
 * <p>
 * Contrato para registrar una ENTRADA o SALIDA de stock en una Bodega.
 * <p>
 * Si el movimiento viola la invariante BOD-05 (stock negativo), el Dominio lanzará
 * {@code StockInsuficienteException}. La Application Layer no la captura —
 * fluye hacia el adaptador REST que la traduce a RFC 7807 Problem Details.
 * <p>
 * Reglas validadas: REGLA-1, REGLA-2, BOD-03, BOD-04, BOD-05, MT-01.
 */
public interface RegistrarMovimientoUseCase {

    /**
     * Ejecuta el registro de un movimiento de inventario.
     *
     * @param command Comando con todos los datos del movimiento.
     *                El {@code empresaId} debe provenir del JWT (nunca del cliente).
     * @return        {@code MovimientoResponse} con la confirmación y stock resultante.
     * @throws com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.StockInsuficienteException
     *                Si el movimiento es SALIDA y deja el stock negativo (BOD-05).
     * @throws jakarta.persistence.EntityNotFoundException
     *                Si la Bodega o el Producto no existen para el tenant dado.
     */
    MovimientoResponse ejecutar(RegistrarMovimientoCommand command);
}
