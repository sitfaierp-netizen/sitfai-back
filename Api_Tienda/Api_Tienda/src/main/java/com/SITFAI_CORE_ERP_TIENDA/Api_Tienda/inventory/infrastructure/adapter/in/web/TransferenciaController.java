package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.TransferirStockCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.input.TransferirStockUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto.TransferirStockWebRequest;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web.TenantId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

/**
 * Controlador REST (Driving Adapter) para gestionar las transferencias de stock (Logística Inversa).
 * <p>
 * Implementa el protocolo Zero Trust: el 'empresaId' nunca se confía desde el request body
 * ni desde headers no firmados, sino que es inyectado desde los claims validados del JWT 
 * mediante la anotación {@link TenantId} (REGLA-4, REGLA-7, MT-01).
 */
@RestController
@RequestMapping("/inventory/transferencias")
public class TransferenciaController {

    private final TransferirStockUseCase transferirStockUseCase;

    public TransferenciaController(TransferirStockUseCase transferirStockUseCase) {
        this.transferirStockUseCase = Objects.requireNonNull(transferirStockUseCase, "transferirStockUseCase no puede ser null");
    }

    /**
     * Endpoint para registrar una transferencia de stock entre dos bodegas.
     * POST /api/v1/inventory/transferencias
     */
    @PostMapping
    public ResponseEntity<Void> transferirStock(
            @TenantId UUID empresaId,
            @RequestBody TransferirStockWebRequest request) {

        // Mapeo manual simple para mantener cero dependencias en el DTO (puro Java 25)
        TransferirStockCommand command = new TransferirStockCommand(
                empresaId,
                request.bodegaOrigenId(),
                request.bodegaDestinoId(),
                request.productoId(),
                request.cantidad(),
                request.documentoFuente()
        );

        transferirStockUseCase.ejecutar(command);

        // Devolvemos 204 No Content, o 200/201 según convención. 201 Created es adecuado para recursos transaccionales, 
        // pero como no se retorna recurso en particular, 204 es limpio o podemos enviar un 200 OK.
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
