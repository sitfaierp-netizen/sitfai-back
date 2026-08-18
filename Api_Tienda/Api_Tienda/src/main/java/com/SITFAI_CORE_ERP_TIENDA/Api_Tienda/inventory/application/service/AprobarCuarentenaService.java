package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.AprobarCuarentenaCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarMovimientoCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.AprobarCuarentenaUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.RegistrarMovimientoUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Application Service para orquestar la resolución de cuarentena.
 * Aplica regla BOD-06 (Transferencia explícita doble si es aprobado).
 */
@Service
public class AprobarCuarentenaService implements AprobarCuarentenaUseCase {

    private final RegistrarMovimientoUseCase registrarMovimientoUseCase;

    public AprobarCuarentenaService(RegistrarMovimientoUseCase registrarMovimientoUseCase) {
        this.registrarMovimientoUseCase = registrarMovimientoUseCase;
    }

    @Override
    @Transactional
    public void aprobarCuarentena(AprobarCuarentenaCommand command) {
        if (!command.aprobado()) {
            // Lógica futura: Merma, destrucción o devolución al proveedor.
            // Por ahora, si es falso, no la transferimos al stock vendible.
            return;
        }

        String bodegaCuarentenaId = command.sucursalId() + "-CUARENTENA";
        String bodegaMatrizId = command.sucursalId(); // Asumiendo que el ID de la bodega principal es igual al sucursalId o requiere resolución
        String docFuente = "INSPECCION-" + UUID.randomUUID().toString().substring(0, 8);

        // Regla BOD-06: 1. SALIDA de Cuarentena
        RegistrarMovimientoCommand salida = new RegistrarMovimientoCommand(
                command.empresaId(),
                bodegaCuarentenaId,
                command.productoId(),
                command.cantidad(),
                "SALIDA",
                "INSPECCION_CALIDAD",
                docFuente
        );
        registrarMovimientoUseCase.ejecutar(salida);

        // Regla BOD-06: 2. ENTRADA a Bodega Principal (Stock Vendible)
        RegistrarMovimientoCommand entrada = new RegistrarMovimientoCommand(
                command.empresaId(),
                bodegaMatrizId,
                command.productoId(),
                command.cantidad(),
                "ENTRADA",
                "INSPECCION_CALIDAD",
                docFuente
        );
        registrarMovimientoUseCase.ejecutar(entrada);
    }
}
