# Bounded Context: POS (Punto de Venta)
> **Estado:** 🟢 COMPLETADO Y VALIDADO (TurnoCaja y Arqueo Financiero) | **Puerto Asignado:** 8086

## Propósito
Gestionar las operaciones financieras y transaccionales en el punto de venta físico, incluyendo el control financiero de las cajas registradoras, apertura de turnos, registro de movimientos, cierres de turno y arqueo inmutable de caja con aislamiento multitenant estricto (MT-01).

## Capas
- [x] Domain Layer (`TurnoCaja`, `TransaccionCaja`, `ArqueoCaja`, Value Objects con `RoundingMode.HALF_UP`, Invariantes CAJ-02 a CAJ-07 verificados)
- [x] Application Layer (`GestionarTurnoCajaService`, `AbrirTurnoCommand`, `CerrarTurnoCommand`, `TurnoCajaResponse`, Casos de Uso)
- [x] Infrastructure Layer (`TurnoCajaJpaEntity` heredando de `AuditableJpaEntity` [AUD-01], `TurnoCajaJpaAdapter`, `TurnoCajaController`, Flyway `V44__init_pos_schema.sql`)

## Modelo de Dominio — Agregado TurnoCaja
- **Aggregate Root:** `TurnoCaja` (`com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.TurnoCaja`)
- **Entidades Internas:** `TransaccionCaja` (movimientos de caja inmutables con documento fuente obligatorio BOD-04)
- **Value Objects:**
  - `TurnoId`: Identificador único del turno.
  - `CajaId`: Identificador de la caja física/lógica.
  - `CajeroId`: Identificador del operador responsable.
  - `EmpresaId`: Aislamiento multi-tenant obligatorio (MT-01).
  - `Dinero`: Encapsulación de `BigDecimal` con precisión decimal de 4 dígitos y redondeo `RoundingMode.HALF_UP` (MONEY-01).
  - `EstadoTurno`: `ABIERTO`, `CERRADO`.
  - `TipoTransaccionCaja`: `VENTA`, `DEVOLUCION`, `INGRESO`, `EGRESO`.
  - `ArqueoCaja`: Resumen financiero inmutable (monto apertura, ventas, ingresos, devoluciones, egresos, total teórico, monto físico declarado, descuadre, fecha cierre).

## Reglas de Negocio Implementadas
- **CAJ-02:** Apertura de turno con monto inicial en estado `ABIERTO`.
- **CAJ-03:** Invariante fail-fast: Solo se pueden registrar transacciones mientras el turno esté en estado `ABIERTO`.
- **CAJ-04:** Tipos de transacciones de caja estandarizadas: `VENTA`, `DEVOLUCION`, `INGRESO`, `EGRESO`.
- **CAJ-05:** Balance teórico esperado: `montoApertura + VENTA + INGRESO - DEVOLUCION - EGRESO`.
- **CAJ-06:** Cierre de turno mediante declaración física de efectivo (`montoFisicoDeclarado`).
- **CAJ-07:** Cálculo de descuadre (`declarado - esperado`), transición a `CERRADO` y emisión inmutable de `TurnoCerradoEvent`.
- **DOC-01:** Sellado inmutable del arqueo financiero tras el cierre del turno.
- **AUD-01:** Herencia de `AuditableJpaEntity` asegurando `creado_en`, `creado_por`, `actualizado_en`, `actualizado_por`.

## Puertos y Adaptadores
- **Input Ports:**
  - `GestionarTurnoCajaUseCase`, `AbrirTurnoUseCase`, `CerrarTurnoUseCase` implementados en `GestionarTurnoCajaService`.
- **Output Ports:**
  - `TurnoCajaRepository` implementado por `TurnoCajaJpaAdapter` (`posTurnoCajaJpaAdapter`).
  - `TenantProviderPort` y `CurrentActorProvider` en infraestructura de seguridad.
- **Driving Adapter (REST):**
  - `TurnoCajaController` (`/api/v1/pos/turnos`):
    - `POST /api/v1/pos/turnos/abrir`: Apertura de turno con monto base.
    - `POST /api/v1/pos/turnos/{id}/cerrar`: Cierre de turno con arqueo inmutable.

## Base de Datos (Flyway)
- `V44__init_pos_schema.sql`: Migración que añade auditoría transaccional (`AuditableJpaEntity`), campos de arqueo (`monto_cierre`, `total_teorico`, `descuadre`) e índices de rendimiento en `pos_turno_caja` y `pos_transaccion_caja`.
