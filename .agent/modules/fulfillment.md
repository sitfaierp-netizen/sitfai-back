# Módulo Fulfillment (Preparación y Despacho)
> **Versión:** 1.0.0 | **Estado:** DISEÑO DOMINIO

## Responsabilidad
Encapsula las reglas de negocio sobre la preparación y logística de salida de pedidos (picking, packing y despacho). 

## Estructura Actual
- **Dominio:** 
  - **Aggregates:** `OrdenDespacho`
  - **Local Entities:** `LineaDespacho`
  - **Value Objects:** `DespachoId`, `BodegaId`, `EmpresaId`, `PedidoId`, `EstadoDespacho`, `ProductoId`
  - **Ports:** `OrdenDespachoRepository`
  - **Eventos:** `DespachoCompletadoEvent`

## Eventos de Dominio (Outbound)
- `DespachoCompletadoEvent`: Emitido al completarse el empaque y confirmarse el despacho para descontar stock físico final.

## Reglas Inquebrantables del Dominio
- Protección del Ciclo de Vida: El estado del despacho debe ser obligatoriamente secuencial: PENDIENTE -> EN_PICKING -> EMPACADO -> DESPACHADO. No se admiten saltos lógicos.
- Cero dependencias de framework en la capa Domain.
