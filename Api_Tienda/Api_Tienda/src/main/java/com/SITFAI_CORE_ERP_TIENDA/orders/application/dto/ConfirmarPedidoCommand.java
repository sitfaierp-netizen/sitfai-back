package com.SITFAI_CORE_ERP_TIENDA.orders.application.dto;

import java.util.UUID;

public record ConfirmarPedidoCommand(
        UUID pedidoId,
        UUID empresaId
) {}
