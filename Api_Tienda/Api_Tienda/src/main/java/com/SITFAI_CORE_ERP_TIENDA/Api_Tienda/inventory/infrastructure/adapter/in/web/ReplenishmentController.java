package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ConfigurarPuntoReordenCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.ConfigurarPuntoReordenUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.ConfigurarPuntoReordenWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web.TenantId;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

/**
 * Adaptador de Entrada (Driving Adapter): Controlador REST para Replenishment (Punto de Reorden).
 * <p>
 * Seguridad Zero Trust: Utiliza @TenantId para extraer de forma segura el inquilino desde el JWT
 * en lugar de confiar en cabeceras HTTP manipulables (REGLA-4, MT-01).
 */
@RestController
@RequestMapping("/inventory/bodegas")
public class ReplenishmentController {

    private final ConfigurarPuntoReordenUseCase useCase;

    public ReplenishmentController(ConfigurarPuntoReordenUseCase useCase) {
        this.useCase = Objects.requireNonNull(useCase, "useCase no puede ser nulo");
    }

    /**
     * Configura el punto de reorden (BOD-08) para un producto en la bodega.
     */
    @PatchMapping("/{id}/punto-reorden")
    public ResponseEntity<Void> configurarPuntoReorden(
            @PathVariable("id") UUID bodegaId,
            @TenantId UUID empresaId,
            @RequestBody ConfigurarPuntoReordenWebRequest request) {

        ConfigurarPuntoReordenCommand command = new ConfigurarPuntoReordenCommand(
                empresaId,
                bodegaId,
                request.productoId(),
                request.puntoReorden()
        );

        useCase.ejecutar(command);

        return ResponseEntity.noContent().build();
    }
}
