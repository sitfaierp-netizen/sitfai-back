package com.SITFAI_CORE_ERP_TIENDA.shared.event;

import java.util.UUID;

/**
 * Evento de Integración (Shared Kernel) emitido tras el registro de un Tenant (Empresa).
 * Permite a otros módulos aprovisionar sus recursos sin acoplarse a los Value Objects de core_empresa.
 */
public record EmpresaRegistradaIntegrationEvent(
        UUID empresaId,
        UUID sucursalMatrizId,
        String ruc,
        String razonSocial
) {}
