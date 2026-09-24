package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.AnalisisAbcResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.EjecutarAnalisisAbcCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.EjecutarAnalisisAbcUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

/**
 * Driving Adapter: REST Controller para el disparador del Análisis ABC de Inventario.
 * <p>
 * Regla 5 (API REST): Exposición HTTP de la operación bajo la ruta canónica relativa.
 * Ruta: POST /inventory/bodegas/{id}/analisis-abc
 * Regla MT-02: No acepta empresa_id en el payload ni en la URL; se extrae criptográficamente en el caso de uso.
 */
@RestController
@RequestMapping("/inventory/bodegas")
public class AnalisisAbcController {

    private final EjecutarAnalisisAbcUseCase useCase;

    public AnalisisAbcController(EjecutarAnalisisAbcUseCase useCase) {
        this.useCase = Objects.requireNonNull(useCase, "EjecutarAnalisisAbcUseCase no puede ser nulo");
    }

    /**
     * Dispara manualmente el cálculo del Análisis ABC para todos los productos de la bodega.
     *
     * @param bodegaId Identificador único de la bodega.
     * @return Resumen consolidado del resultado de la clasificación.
     */
    @PostMapping("/{id}/analisis-abc")
    public ResponseEntity<AnalisisAbcResponse> ejecutarAnalisisAbc(@PathVariable("id") UUID bodegaId) {
        EjecutarAnalisisAbcCommand command = new EjecutarAnalisisAbcCommand(bodegaId);
        AnalisisAbcResponse response = useCase.ejecutar(command);
        return ResponseEntity.ok(response);
    }
}
