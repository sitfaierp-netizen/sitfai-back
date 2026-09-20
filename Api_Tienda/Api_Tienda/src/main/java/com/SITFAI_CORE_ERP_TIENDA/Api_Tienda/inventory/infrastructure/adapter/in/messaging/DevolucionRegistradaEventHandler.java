package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ReingresarStockPorDevolucionCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.ReingresarStockPorDevolucionUseCase;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.DevolucionRegistradaEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class DevolucionRegistradaEventHandler {

    private final ReingresarStockPorDevolucionUseCase useCase;

    public DevolucionRegistradaEventHandler(ReingresarStockPorDevolucionUseCase useCase) {
        this.useCase = useCase;
    }

    @EventListener
    public void onDevolucionRegistrada(DevolucionRegistradaEvent event) {
        List<ReingresarStockPorDevolucionCommand.LoteRevertidoDto> lotes = event.lotesRevertidos().stream()
                .map(l -> new ReingresarStockPorDevolucionCommand.LoteRevertidoDto(
                        l.productoId().value(),
                        l.codigoLote(),
                        l.cantidad()
                ))
                .collect(Collectors.toList());

        ReingresarStockPorDevolucionCommand command = new ReingresarStockPorDevolucionCommand(
                event.empresaId().value(),
                event.sucursalId().value(),
                event.ventaOrigenId(),
                lotes
        );

        useCase.ejecutar(command);
    }
}
