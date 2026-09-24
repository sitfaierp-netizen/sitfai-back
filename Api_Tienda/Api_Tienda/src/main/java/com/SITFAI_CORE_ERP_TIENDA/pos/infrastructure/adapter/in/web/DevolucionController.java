package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.ProcesarDevolucionCommand;
import com.SITFAI_CORE_ERP_TIENDA.pos.application.port.input.ProcesarDevolucionUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pos/turnos")
public class DevolucionController {

    private final ProcesarDevolucionUseCase procesarDevolucionUseCase;

    public DevolucionController(ProcesarDevolucionUseCase procesarDevolucionUseCase) {
        this.procesarDevolucionUseCase = procesarDevolucionUseCase;
    }

    @PostMapping("/{turnoId}/devoluciones")
    public ResponseEntity<Void> procesarDevolucion(
            @PathVariable UUID turnoId,
            @RequestBody com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.in.web.dto.ProcesarDevolucionWebRequest request) {
        
        List<com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.LineaDevolucionDto> lineas = request.lineas().stream()
                .map(l -> new com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.LineaDevolucionDto(l.productoId(), l.cantidad(), l.precioUnitario()))
                .toList();
                
        List<com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.LoteRevertidoDto> lotes = request.lotesRevertidos().stream()
                .map(l -> new com.SITFAI_CORE_ERP_TIENDA.pos.application.dto.LoteRevertidoDto(l.productoId(), l.codigoLote(), l.cantidad()))
                .toList();

        ProcesarDevolucionCommand command = new ProcesarDevolucionCommand(
                turnoId,
                request.ticketOriginalId(),
                request.montoDevuelto(),
                lineas,
                lotes,
                request.version()
        );

        procesarDevolucionUseCase.ejecutar(command);

        // Retornar 202 Accepted o 200 OK
        return ResponseEntity.ok().build();
    }
}
