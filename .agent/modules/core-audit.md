# Módulo: core_audit — Event Store y Outbox Dispatcher Transversal
> **Estado:** 🟢 SISTEMA COMPLETO Y CERTIFICADO (Event Store + Outbox Poller) | **Fecha:** 2026-09-22 | **Revisión:** 1.2.0

---

## Bounded Context

**Nombre:** `core_audit`  
**Paquete raíz:** `com.SITFAI_CORE_ERP_TIENDA.core_audit`  
**Responsabilidad:** Motor transversal de auditoría, trazabilidad forense, persistencia inmutable de Eventos de Dominio (Event Store) y despacho asíncrono garantizado (Outbox Pattern Poller) para coreografía de eventos en el ecosistema SITFAI ERP.

---

## Reglas de Negocio Implementadas

| ID | Regla | Implementación en Dominio / Aplicación / Infra |
|---|---|---|
| **AUD-03** | Los eventos de dominio se persisten inmutablemente en una tabla de Event Store. | Agregado `StoredDomainEvent` notariado, `NotariarEventoService` (serialización JSON vía Jackson), tabla `core_audit_event_store` (LONGTEXT payload) y migración `V41`. |
| **MT-01** | Aislamiento multi-tenant inquebrantable mediante `empresa_id`. | Value Object inmutable `EmpresaId` encapsulado en agregado, comando, entidad JPA (`BINARY(16)`), índice compuesto `uq_core_audit_event_store_empresa_id` y firmas de consulta filtradas. |
| **OUTBOX-1** | Despacho asíncrono y fase BEFORE_COMMIT sin bloqueo transaccional. | `GlobalDomainEventAuditListener` con `@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)` garantizando sellado en la misma transacción antes del commit. |
| **OUTBOX-2** | Entrega garantizada (At-least-once) mediante Poller periódico. | `OutboxEventPoller` (`@Scheduled`), `ProcesarEventosPendientesService` y `EventDispatcherAdapter` con reenvío de `OutboxMessageEvent` y actualización transaccional de estado `PROCESADO`/`FALLIDO`. |

---

## Capas y Componentes Arquitectónicos

### 1. Dominio Puro (`core_audit.domain`)
- **Value Objects:** `StoredEventId` (UUID global), `EmpresaId` (MT-01), `EventStatus` (PENDIENTE, PROCESADO, FALLIDO).
- **Aggregate Root:** `StoredDomainEvent` (sellado de payload, `notariar(...)`, `reconstituir(...)`, transiciones `marcarProcesado()` y `marcarFallido()`).
- **Driven Ports:** 
  - `EventStoreRepository`: persistencia y recuperación de eventos (incluyendo `buscarPendientes(int limite)`).
  - `EventDispatcherPort`: contrato para el despacho de eventos hacia el bus de integración.

### 2. Aplicación (`core_audit.application`)
- **DTOs:**
  - `NotariarEventoCommand`: comando para registrar eventos en el Event Store.
  - `ProcesarEventosPendientesCommand` y `ProcesarEventosResponse`: comandos y respuestas métricas del Outbox.
- **Driving Ports:**
  - `NotariarEventoUseCase`: caso de uso de notarización.
  - `ProcesarEventosPendientesUseCase`: caso de uso del Outbox Dispatcher.
- **Servicios:**
  - `NotariarEventoService` (`@Service`, `@Transactional`): serialización Jackson y persistencia inmutable inicial.
  - `ProcesarEventosPendientesService` (`@Service`, `@Transactional`): recuperación de lotes PENDIENTES, despacho vía `EventDispatcherPort` y transición controlada a `PROCESADO` o `FALLIDO`.

### 3. Infraestructura (`core_audit.infrastructure`)
- **El Gran Oyente:** `GlobalDomainEventAuditListener` (`@TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT, fallbackExecution = true)`). Atrapa cualquier `DomainEvent`, extrae el `empresa_id` de forma genérica y lo remite al caso de uso.
- **El Motor Cron (Poller):** `OutboxEventPoller` con `@Scheduled(fixedDelay = 5000)` para ejecución periódica y `OutboxSchedulingConfig` con `@EnableScheduling`.
- **El Despachador:** `EventDispatcherAdapter` implementando `EventDispatcherPort`, utilizando `ApplicationEventPublisher` y publicando `OutboxMessageEvent`.
- **Entidad JPA:** `StoredEventJpaEntity` extendiendo de `AuditableJpaEntity` (AUD-01), mapeando `id` y `empresa_id` en `BINARY(16)`, `payload` en `LONGTEXT` y `estado` en `VARCHAR(30)`.
- **Spring Data Repository:** `StoredEventJpaRepository` con `findByEstadoOrderByOcurridoEnAsc(...)`.
- **Mapper:** `EventStoreMapper`.
- **Driven Adapter:** `EventStoreJpaAdapter` implementando `EventStoreRepository`.
- **Base de Datos:** Script Flyway `V41__init_event_store_schema.sql` creando la tabla `core_audit_event_store` con índices de partición multitenant y optimización para el Outbox poller.

---

## Matriz de Verificación Automatizada

- `StoredDomainEventTest.java`: 17 tests unitarios de Dominio (100% éxito).
- `NotariarEventoServiceTest.java`: 3 tests unitarios de Aplicación con mocks (100% éxito).
- `GlobalDomainEventAuditListenerTest.java`: 4 tests unitarios de Infraestructura (100% éxito).
- `ProcesarEventosPendientesServiceTest.java`: 3 tests unitarios de Aplicación para el Outbox Dispatcher (100% éxito).
- `EventDispatcherAdapterTest.java`: 1 test unitario del despachador con `ApplicationEventPublisher` (100% éxito).
- `OutboxEventPollerTest.java`: 1 test unitario del Scheduler (100% éxito).
- `shared...GlobalDomainEventAuditListenerTest.java`: 2 tests (100% éxito).
- **Total Tests Unitarios Ejecutados:** **31 tests, 0 fallos, 0 errores (100% PASS)**.
- `EventStoreIntegrationTest.java` / `OutboxIntegrationTest.java`: Pruebas de integración con Testcontainers y MySQL 8.4 LTS.
- `mvnw clean test-compile`: **BUILD SUCCESS** sobre los 772 archivos fuente del backend.
