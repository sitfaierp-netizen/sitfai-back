package com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web.mapper;

import com.SITFAI_CORE_ERP_TIENDA.billing.application.dto.EmitirFacturaCommand;
import com.SITFAI_CORE_ERP_TIENDA.billing.infrastructure.adapter.in.web.dto.EmitirFacturaRequest;

import java.util.UUID;

/**
 * Mapper Web: Convierte DTOs Web a Comandos de Aplicación.
 */
public final class FacturaWebMapper {

    private FacturaWebMapper() {}

    public static EmitirFacturaCommand toCommand(UUID empresaId, EmitirFacturaRequest request) {
        return new EmitirFacturaCommand(
                empresaId,
                request.clienteId(),
                request.pedidoId(),
                request.rucCliente(),
                request.lineas().stream()
                        .map(l -> new EmitirFacturaCommand.LineaFacturaCommand(
                                l.concepto(),
                                l.cantidad(),
                                l.precioUnitario(),
                                l.moneda(),
                                l.impuestos() != null ? l.impuestos().stream()
                                        .map(i -> new EmitirFacturaCommand.ImpuestoCommand(i.tipo(), i.tarifa()))
                                        .toList() : null
                        ))
                        .toList()
        );
    }
}
