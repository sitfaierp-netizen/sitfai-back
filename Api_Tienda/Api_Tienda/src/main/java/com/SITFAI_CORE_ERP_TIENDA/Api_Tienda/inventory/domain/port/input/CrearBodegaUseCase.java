package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.BodegaResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.CrearBodegaCommand;

/**
 * Puerto de Entrada (Driving Port) — Caso de Uso: Crear Bodega.
 * <p>
 * Define el contrato que el adaptador REST (o cualquier otro adaptador de entrada)
 * debe usar para disparar el caso de uso de creación de una Bodega.
 * <p>
 * La implementación concreta está en {@code CrearBodegaService} (Application Layer).
 * Los adaptadores de entrada (REST Controller) conocen SOLO esta interface,
 * nunca la implementación concreta (Hexagonal Architecture).
 * <p>
 * Reglas validadas: REGLA-1 (Driving Port en domain/port/input),
 * REGLA-2 (estructura de paquetes), BOD-01, BOD-02, MT-01.
 */
public interface CrearBodegaUseCase {

    /**
     * Ejecuta el caso de uso de creación de una Bodega.
     *
     * @param command Comando con los datos necesarios para crear la Bodega.
     *                El {@code empresaId} debe ser extraído del JWT por el adaptador REST.
     * @return        {@code BodegaResponse} con los datos de la Bodega creada.
     * @throws com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.exception.DomainException
     *                Si se viola alguna regla de negocio del dominio.
     * @throws IllegalArgumentException Si el código de Bodega ya existe en la Sucursal (BOD-02).
     */
    BodegaResponse ejecutar(CrearBodegaCommand command);
}
