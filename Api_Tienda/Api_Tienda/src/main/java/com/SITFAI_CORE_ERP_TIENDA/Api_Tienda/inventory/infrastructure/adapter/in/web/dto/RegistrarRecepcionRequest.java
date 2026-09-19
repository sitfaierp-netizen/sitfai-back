package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.infrastructure.adapter.in.web.dto;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.LineaRecepcionDto;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.LoteDto;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.RegistrarRecepcionCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record RegistrarRecepcionRequest(
        @NotNull UUID bodegaDestinoId,
        @NotNull UUID ordenCompraOrigenId,
        @NotEmpty @Valid List<LineaRecepcionRequest> lineas
) {
    public RegistrarRecepcionCommand toCommand() {
        List<LineaRecepcionDto> lineasDto = lineas.stream()
                .map(linea -> new LineaRecepcionDto(
                        linea.productoId(),
                        linea.cantidad(),
                        new LoteDto(linea.lote().codigoLote(), linea.lote().fechaCaducidad())
                ))
                .collect(Collectors.toList());

        return new RegistrarRecepcionCommand(
                bodegaDestinoId,
                ordenCompraOrigenId,
                lineasDto
        );
    }

    public record LineaRecepcionRequest(
            @NotNull UUID productoId,
            @NotNull @Positive BigDecimal cantidad,
            @NotNull @Valid LoteRequest lote
    ) {
    }

    public record LoteRequest(
            @NotEmpty String codigoLote,
            @NotNull LocalDate fechaCaducidad
    ) {
    }
}
