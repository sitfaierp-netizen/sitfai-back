# Módulo Punto de Venta (POS)

## 1. Lenguaje Ubicuo (Ubiquitous Language)
- **Turno de Caja (`TurnoCaja`):** Periodo de tiempo durante el cual un operario (cajero) gestiona una caja registradora. Mantiene un balance (arqueo) del efectivo.
- **Ticket de Venta (`TicketVenta`):** Documento comercial que respalda la venta de productos al cliente final.
- **Transacción de Caja (`TransaccionCaja`):** Movimientos monetarios dentro del turno. Tipos: INGRESO, EGRESO, VENTA, DEVOLUCION.
- **Estado de Turno (`EstadoTurno`):** Ciclo de vida del turno (`ABIERTO`, `CERRADO`, `DESCUADRADO`).
- **Arqueo (`ArqueoCaja`):** Snapshot de los fondos esperados vs reales en una caja.

## 2. Agregados (Aggregates)
- **`TurnoCaja`:** Orquesta la apertura, registro de transacciones (ventas, ingresos, egresos) y el cierre de caja asegurando la validación del balance final contra el esperado.
- **`TicketVenta`:** Entidad que contiene las líneas de ticket (`LineaTicket`), métodos de pago (`MetodoPago`) y controla la emisión del comprobante (VentaRegistrada).

## 3. Value Objects
- `CajaId`, `CajeroId`, `TurnoId`, `EmpresaId`, `SucursalId`, `TicketId`, `TransaccionId`
- `Dinero` (moneda y monto)
- `EstadoTurno` (ABIERTO, CERRADO, DESCUADRADO)
- `TipoTransaccionCaja` (INGRESO, EGRESO, VENTA, DEVOLUCION)
- `ArqueoCaja`

## 4. Domain Events
- `TurnoAbiertoEvent`
- `TurnoCerradoEvent`
- `VentaRegistradaEvent` (Comunica la venta final a Inventario y Billing)
- `DevolucionRegistradaEvent` (Dispara la cuarentena en el inventario)
- `TicketEmitidoEvent`

## 5. Puertos (Ports)
- `TurnoCajaRepository`
- `TicketVentaRepository`
