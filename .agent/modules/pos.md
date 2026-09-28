# Módulo Punto de Venta (POS)
> **Versión:** 2.1.0 | **Fecha Auditoría:** 2026-09-28 | **Puerto:** `8086` | **Estado:** 🟢 DOMINIO CONSOLIDADO Y REFRACTORIZADO (GAP-01 Y GAP-02 CERRADOS)

---

## 1. Lenguaje Ubicuo (Ubiquitous Language)

| Término | Definición |
|---------|-----------|
| **Turno de Caja** (`TurnoCaja`) | Periodo de tiempo durante el cual un cajero gestiona una caja registradora. Es la Aggregate Root principal del módulo. |
| **Ticket de Venta** (`TicketVenta`) | Documento comercial inmutable que respalda la venta de productos al cliente final. Segunda Aggregate Root. |
| **Transacción de Caja** (`TransaccionCaja`) | Movimiento financiero dentro del turno. Exige documento fuente (BOD-04). |
| **Arqueo de Caja** (`ArqueoCaja`) | Snapshot financiero inmutable generado al cierre: Σ(ventas + ingresos - devoluciones - egresos) vs monto físico declarado. |
| **Estado de Turno** (`EstadoTurno`) | Ciclo de vida del turno: `ABIERTO → CERRADO`. |
| **Tipo de Transacción** (`TipoTransaccionCaja`) | `VENTA`, `DEVOLUCION`, `INGRESO`, `EGRESO`. |
| **Cajero** (`CajeroId`) | Identificador fuertemente tipado del operario de caja. |
| **Descuadre** | Diferencia entre el total teórico esperado y el monto físico declarado. Puede ser sobrante (+) o faltante (-). |
| **Método de Pago** (`MetodoPago`) | Forma de pago del ticket (efectivo, tarjeta, etc.). |

---

## 2. Agregados (Aggregates)

### Aggregate Root 1: `TurnoCaja`
**Paquete canónico:** `pos.domain.model.turno`

- **Invariantes:**
  - Solo admite `registrarTransaccion()` en estado `ABIERTO` (fail-fast).
  - El monto de apertura no puede ser negativo.
  - El monto de transacción siempre mayor a cero.
  - Al ejecutar `cerrar()`, el estado pasa a `CERRADO` y es inmutable (DOC-01).
- **Factory Methods:** `abrir(...)`, `reconstituir(...)`
- **Métodos de negocio:** `registrarTransaccion()`, `cerrar()`, `calcularTotalTeorico()`
- **Entidades internas:** `TransaccionCaja`, `ArqueoCaja` (como `record`)
- **Emite:** `TurnoCerradoEvent`

### Aggregate Root 2: `TicketVenta`
**Paquete canónico:** `pos.domain.model` (raíz)

- **Invariantes:**
  - Solo se puede pagar un ticket en estado `BORRADOR`.
  - No se puede pagar un ticket sin líneas (CAJ-01).
  - Estado inmutable una vez `EMITIDO` o `ANULADO` (DOC-01).
- **Factory Methods:** `abrir(...)`, `reconstituir(...)`
- **Métodos de negocio:** `agregarLinea()`, `pagar()`, `anular()`
- **Emite:** `TicketEmitidoEvent`

---

## 3. Value Objects

| Value Object | Paquete Canónico | Tipo Java | Observación |
|---|---|---|---|
| `TurnoId` | `pos.domain.model.turno.vo` | `record` | UUID fuertemente tipado |
| `CajaId` | `pos.domain.model.turno.vo` | `record` | UUID fuertemente tipado |
| `CajeroId` | `pos.domain.model.turno.vo` | `record` | UUID fuertemente tipado |
| `EmpresaId` | `pos.domain.model.turno.vo` | `record` | Clave multitenant (MT-01) |
| `Dinero` | `pos.domain.model.turno.vo` | `record` | BigDecimal DECIMAL(19,4), RoundingMode.HALF_UP |
| `EstadoTurno` | `pos.domain.model.turno.vo` | `enum` | ABIERTO, CERRADO |
| `TipoTransaccionCaja` | `pos.domain.model.turno.vo` | `enum` | VENTA, DEVOLUCION, INGRESO, EGRESO |
| `ArqueoCaja` | `pos.domain.model.turno` | `record` | Inmutable, contiene descuadre |
| `TicketId` | `pos.domain.model.vo` | class | ID del Ticket de Venta |
| `ProductoId` | `pos.domain.model.vo` | class | Referencia cruzada por ID (REGLA-3) |
| `MetodoPago` | `pos.domain.model` | `enum` | Forma de pago |


---

## 4. Eventos de Dominio

| Evento | Emitido por | Consumidores | Descripción |
|--------|------------|--------------|-------------|
| `TurnoAbiertoEvent` | `TurnoCaja` | — | Apertura de turno |
| `TurnoCerradoEvent` | `TurnoCaja.cerrar()` | `billing`, `audit` | Arqueo financiero inmutable al cierre |
| `VentaRegistradaEvent` | — | `inventory`, `billing` | Dispara deducción FEFO en stock y facturación |
| `TicketEmitidoEvent` | `TicketVenta.pagar()` | `inventory`, `billing` | Ticket pagado con líneas FEFO |
| `DevolucionRegistradaEvent` | `TurnoCaja.registrarDevolucion()` | `inventory` | Dispara cuarentena en bodega |

---

## 5. Puertos de Dominio (Domain Ports)

### Output Ports (Driven — Domain Layer)
| Puerto | Paquete | Responsabilidad |
|--------|---------|-----------------|
| `TurnoCajaRepository` | `pos.domain.port.output` | CRUD + búsqueda por tenant del Turno |
| `TicketVentaRepository` | `pos.domain.port.output` | CRUD + búsqueda por tenant del Ticket |

### Input Ports (Use Cases — Application Layer)
| Puerto | Descripción |
|--------|-------------|
| `AbrirTurnoUseCase` | Apertura de turno con monto inicial |
| `CerrarTurnoUseCase` | Cierre y arqueo de turno |
| `RegistrarTransaccionCajaUseCase` | Registro de movimientos financieros |
| `ProcesarDevolucionUseCase` | Procesamiento de devoluciones |
| `CrearCajaUseCase` | Alta de caja registradora |
| `ConsultarCajasUseCase` | Consulta de cajas disponibles |
| `ConsultarTurnoUseCase` | Consulta de estado de turno |
| `GestionarTurnoCajaUseCase` | Orquestación de gestión de turno |

---

## 6. Excepciones de Dominio

| Excepción | Descripción |
|-----------|-------------|
| `DomainException` | Base abstract de excepciones de dominio |
| `CajaInvalidaException` | Caja no válida o ya tiene turno abierto |
| `TurnoInvalidoException` | Operación inválida sobre el turno |
| `TurnoNoEncontradoException` | Turno no encontrado para el tenant |

---

## 7. Reglas de Negocio del Módulo POS (CAJ)

| ID | Regla |
|----|-------|
| **CAJ-01** | Un ticket debe tener al menos una línea para ser pagado |
| **CAJ-02** | Solo puede existir un turno ABIERTO por caja a la vez |
| **CAJ-03** | Solo se registran transacciones en turnos ABIERTOS (fail-fast) |
| **CAJ-04** | Toda transacción exige `documentoFuenteId` traceable (BOD-04) |
| **CAJ-05** | El arqueo de caja es generado e inmutable al cierre del turno (DOC-01) |
| **CAJ-06** | El monto de apertura y de transacciones no puede ser negativo ni cero |
| **CAJ-07** | El descuadre = montoFisicoDeclarado - totalTeoricoEsperado |

---

## 8. Estado de la Infraestructura

| Componente | Estado | Ubicación |
|-----------|--------|-----------|
| REST Controllers (TurnoCaja) | Presente | `infrastructure/adapter/in/web` |
| REST Controllers (Devolución) | Presente | `infrastructure/adapter/in/web` |
| REST DTOs (Request/Response) | Presente | `infrastructure/adapter/in/web/dto` |
| JPA Adapters (out/persistence) | Presente | `infrastructure/adapter/out/persistence` |
| Spring Event Publisher | Presente | `infrastructure/adapter/out/event` |
| Keycloak Security Adapter | Presente | `infrastructure/adapter/out/security` |
| Event Listener (Messaging in) | Presente | `infrastructure/adapter/in/messaging` |
| Application Services (8 servicios) | Presente | `application/service` |
| Application DTOs/Commands | Presente | `application/dto` |

---

## 9. Deuda Técnica Detectada



### CONFORME — Pureza de Framework

Cero importaciones de `jakarta.persistence`, `org.springframework` o `com.fasterxml.jackson` en la capa de Dominio.
