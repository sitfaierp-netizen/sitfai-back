package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web.mapper;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web.dto.FacturaWebRequest;

import java.util.UUID;

/**
 * Mapper Web: Convierte DTOs Web a Comandos de Aplicación.
 */
public final class FacturaWebMapper {

    private FacturaWebMapper() {}

    public static EmitirFacturaCommand toCommand(UUID empresaId, FacturaWebRequest request) {
        return new EmitirFacturaCommand(
                empresaId,
                request.nitEmisor(),
                request.nitReceptor(),
                request.lineas().stream().map(l -> new EmitirFacturaCommand.LineaFacturaDto(
                        l.concepto(),
                        l.cantidad(),
                        l.precioUnitario(),
                        l.moneda(),
                        l.impuestos().stream().map(i -> new EmitirFacturaCommand.ImpuestoDto(
                                i.tipo(),
                                i.tarifa()
                        )).toList()
                )).toList()
        );
    }
}
