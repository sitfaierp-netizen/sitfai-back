# Bounded Context: Fulfillment (Despachos y WMS)
> **Estado:** 🟢 COMPLETADO Y VALIDADO | **Puerto Asignado:** N/A (Módulo Interno/Futuro Puerto)

## Propósito
Gestionar el ciclo de vida de los despachos físicos (Picking y Packing) de los pedidos comerciales, operando como un Warehouse Management System (WMS) ligero dentro del ERP SITFAI.

## Capas
- [x] Domain Layer (Value Objects, Entities, Aggregate Root `OrdenDespacho`, Invariantes verificados)
- [x] Application Layer (Use Cases, DTOs, Mappers, Ports, Services)
- [x] Infrastructure Layer (JPA Entities, Flyway, Zero Trust REST, RFC 7807)

## Eventos
- **Publica:** `PackingCompletadoEvent` (cuando el packing de la Orden de Despacho alcanza el 100% de los productos solicitados).
- **Consume:** `PedidoConfirmadoEvent` (desde `Api_Tienda`). Altera el estado del sistema inicializando el proceso logístico y planificando el despacho automáticamente.
