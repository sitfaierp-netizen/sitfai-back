# Bounded Context: POS (Punto de Venta)
> **Estado:** 🟢 COMPLETADO Y VALIDADO | **Puerto Asignado:** 8086

## Propósito
Gestionar las operaciones financieras y transaccionales en el punto de venta físico, incluyendo el control de cajas, aperturas, cierres de turno, y arqueo de caja con aislamiento multitenant y multi-sucursal.

## Capas
- [x] Domain Layer (Value Objects, Entities, Aggregate Root `TurnoCaja`, Invariantes verificados)
- [x] Application Layer (Use Cases, DTOs, Mappers, Ports, Services)
- [x] Infrastructure Layer (JPA Entities, Flyway, Zero Trust REST, RFC 7807)

## Eventos
- **Publica:** 
  - `TurnoCerradoEvent` (cuando se cierra y arquea una caja con éxito, consolidando las operaciones del turno).
  - `VentaRegistradaEvent` (cuando se registra una transacción de tipo venta, propagando los productos vendidos).
    - *Integración Coreografiada (Nuevo):* Este evento es escuchado asíncronamente por el módulo `billing` para emitir automáticamente la Factura Electrónica (DIAN), inyectando el NIT "Consumidor Final" por defecto.
- **Consume:**
  - `EmpresaRegistradaIntegrationEvent` (emitido por `core_empresa`): Orquesta la Saga de aprovisionamiento creando automáticamente una Caja Principal para el Tenant.
