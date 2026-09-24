package com.SITFAI_CORE_ERP_TIENDA.replenishment.application.port.input;

import com.SITFAI_CORE_ERP_TIENDA.replenishment.application.dto.EvaluarReposicionCommand;

import java.util.UUID;

/**
 * Puerto de Entrada (Driving Port): Caso de uso que evalúa si se necesita reponer inventario.
 * Regla REGLA-1: Interfaz pura sin dependencias a frameworks.
 */
public interface EvaluarPoliticaInventarioUseCase {
    /**
     * Evalúa la política de inventario activa para un producto en una bodega específica
     * y emite un evento de dominio si se perfora el punto de reorden.
     *
     * @param empresaId ID del tenant (extraído del JWT — MT-01)
     * @param command   Datos de la evaluación
     */
    void evaluar(UUID empresaId, EvaluarReposicionCommand command);
}
