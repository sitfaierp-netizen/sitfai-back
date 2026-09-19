package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.event;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.IncrementarStockCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.MovimientoIngresoDto;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.IncrementarStockUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.RecepcionConfirmadaEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.LineaRecepcion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.Recepcion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.RecepcionRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class IncrementarStockPorRecepcionEventHandler {

    private final IncrementarStockUseCase incrementarStockUseCase;
    private final RecepcionRepository recepcionRepository;

    public IncrementarStockPorRecepcionEventHandler(IncrementarStockUseCase incrementarStockUseCase, RecepcionRepository recepcionRepository) {
        this.incrementarStockUseCase = Objects.requireNonNull(incrementarStockUseCase);
        this.recepcionRepository = Objects.requireNonNull(recepcionRepository);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(RecepcionConfirmadaEvent event) {
        Recepcion recepcion = recepcionRepository.buscarPorIdYEmpresaId(event.recepcionId(), event.empresaId())
                .orElseThrow(() -> new IllegalStateException("Recepción no encontrada al procesar RecepcionConfirmadaEvent"));

        List<MovimientoIngresoDto> movimientos = recepcion.getLineas().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        IncrementarStockCommand command = new IncrementarStockCommand(
                event.empresaId().valor(),
                recepcion.getBodegaDestino().valor(),
                recepcion.getId().valor(),
                movimientos
        );

        incrementarStockUseCase.ejecutar(command);
    }

    private MovimientoIngresoDto mapToDto(LineaRecepcion linea) {
        String codigoLote = linea.getLote() != null ? linea.getLote().codigoLote() : null;
        Instant fechaCaducidad = (linea.getLote() != null && linea.getLote().fechaCaducidad() != null)
                ? linea.getLote().fechaCaducidad().atStartOfDay(ZoneOffset.UTC).toInstant()
                : null;

        return new MovimientoIngresoDto(
                linea.getProductoId().valor(),
                linea.getCantidadRecibida().valor(),
                codigoLote,
                fechaCaducidad
        );
    }
}
