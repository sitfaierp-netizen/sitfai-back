# SITFAI ERP — Core-Empresa (com.SITFAI_CORE_ERP_TIENDA.core_empresa)
> **Versión:** 1.1.0 | **Fecha:** 2026-08-15 | **Estado:** 🟢 COMPLETADO Y VALIDADO

---

## 1. IDENTIDAD DEL MÓDULO

| Campo              | Valor                                                  |
|--------------------|--------------------------------------------------------|
| **Group ID**       | `com.SITFAI_CORE_ERP_TIENDA`                           |
| **Artifact ID**    | `Api_Tienda` (Bounded Context `core_empresa`)          |
| **Package raíz**   | `com.SITFAI_CORE_ERP_TIENDA.core_empresa`              |
| **Puerto Asignado**| `8081`                                                 |
| **Java**           | `21` (Records inmutables, Domain-Driven Design)        |
| **Arquitectura**   | Hexagonal (Clean Architecture / MCP-01)                |
| **BD & Migración** | MySQL 8.0+ / InnoDB / `V5__init_core_empresa_schema.sql`|

---

## 2. OBJETIVO DEL BOUNDED CONTEXT

El módulo `core-empresa` representa el **Tenant Raíz** del SaaS SITFAI ERP. Todos los datos, bodegas, cajas, pedidos y facturas de la plataforma pertenecen a una `Empresa`.

### Invariantes y Reglas Clave Implementadas:
- **EMP-01:** La Empresa es la entidad raíz; es el discriminador obligatorio (`EmpresaId`) en todo el sistema.
- **EMP-02:** Toda Empresa tiene un RUC único e inmutable de 11 dígitos numéricos (`Ruc`).
- **EMP-03:** Una Empresa debe contar con al menos una Sucursal activa para operar (`tieneSucursalActiva()`).
- **EMP-04:** Ciclo de vida: `ACTIVA`, `SUSPENDIDA`, `BAJA`.
- **EMP-06:** Una Empresa suspendida no puede añadir sucursales ni activar operaciones.
- **EMP-07:** La transición a `BAJA` es definitiva y desactiva en cascada todas sus sucursales.
- **SUC-01 / SUC-02:** Las sucursales pertenecen a una Empresa y su código local es único dentro de la misma (case-insensitive).
- **SUC-04 / SUC-06:** Control de estado de Sucursal (`ACTIVA`, `INACTIVA`).
- **MT-02:** Toda consulta de sucursales filtra por `empresaId` del tenant. Ningún endpoint retorna datos de otra Empresa.

---

## 3. ESTRUCTURA COMPLETA DE PAQUETES (MCP-01)

```text
com.SITFAI_CORE_ERP_TIENDA.core_empresa
├── domain/
│   ├── exception/
│   │   ├── DomainException.java
│   │   ├── EmpresaInvalidaException.java
│   │   ├── EmpresaNoEncontradaException.java
│   │   └── RucInvalidoException.java
│   ├── valueobject/
│   │   ├── EmpresaId.java
│   │   ├── SucursalId.java
│   │   ├── Ruc.java
│   │   └── NombreEmpresa.java
│   ├── model/
│   │   ├── EstadoEmpresa.java
│   │   ├── EstadoSucursal.java
│   │   ├── Sucursal.java
│   │   └── Empresa.java
│   └── event/
│       ├── DomainEvent.java
│       ├── EmpresaCreadaEvent.java
│       ├── EmpresaSuspendidaEvent.java
│       ├── EmpresaReactivadaEvent.java
│       ├── EmpresaDadaDeBajaEvent.java
│       ├── SucursalAgregadaEvent.java
│       ├── SucursalDesactivadaEvent.java
│       └── SucursalActivadaEvent.java
│
├── application/
│   ├── dto/
│   │   ├── RegistrarEmpresaCommand.java
│   │   ├── AgregarSucursalCommand.java
│   │   ├── SuspenderEmpresaCommand.java
│   │   ├── DarDeBajaEmpresaCommand.java
│   │   ├── EmpresaResponse.java
│   │   └── SucursalResponse.java
│   ├── mapper/
│   │   └── EmpresaApplicationMapper.java
│   ├── port/
│   │   ├── input/
│   │   │   ├── RegistrarEmpresaUseCase.java
│   │   │   ├── AgregarSucursalUseCase.java
│   │   │   ├── CambiarEstadoEmpresaUseCase.java
│   │   │   ├── ConsultarEmpresaUseCase.java
│   │   │   ├── ObtenerSucursalesPorEmpresaUseCase.java    ← [NUEVO v1.1]
│   │   │   └── CambiarEstadoSucursalUseCase.java          ← [NUEVO v1.1]
│   │   └── output/
│   │       ├── EmpresaRepository.java
│   │       ├── SucursalRepository.java                    ← [NUEVO v1.1]
│   │       └── EmpresaEventPublisher.java
│   └── service/
│       ├── RegistrarEmpresaService.java
│       ├── AgregarSucursalService.java
│       ├── CambiarEstadoEmpresaService.java
│       ├── ConsultarEmpresaService.java
│       ├── ObtenerSucursalesPorEmpresaService.java        ← [NUEVO v1.1]
│       └── CambiarEstadoSucursalService.java              ← [NUEVO v1.1]
│
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── web/
    │   │       ├── dto/
    │   │       │   ├── RegistrarEmpresaRequest.java
    │   │       │   ├── RegistrarEmpresaWebRequest.java
    │   │       │   ├── AgregarSucursalRequest.java
    │   │       │   ├── CambiarEstadoEmpresaRequest.java
    │   │       │   ├── EmpresaWebResponse.java
    │   │       │   └── SucursalWebResponse.java
    │   │       ├── mapper/
    │   │       │   └── EmpresaWebMapper.java
    │   │       ├── EmpresaController.java                 ← [EXPANDIDO v1.1: +3 endpoints]
    │   │       └── CoreEmpresaExceptionHandler.java
    │   └── out/
    │       ├── persistence/
    │       │   ├── entity/
    │       │   │   ├── EmpresaJpaEntity.java
    │       │   │   └── SucursalJpaEntity.java
    │       │   ├── repository/
    │       │   │   ├── EmpresaJpaRepository.java
    │       │   │   └── SucursalJpaRepository.java         ← [NUEVO v1.1]
    │       │   ├── mapper/
    │       │   │   └── EmpresaPersistenceMapper.java
    │       │   ├── EmpresaJpaAdapter.java
    │       │   └── SucursalJpaAdapter.java                ← [NUEVO v1.1]
    │       └── event/
    │           └── SpringEventEmpresaPublisher.java
```

---

## 4. ENDPOINTS REST (BASE: `/api/v1/empresas` vía context-path global)

| Método  | Endpoint                                             | Descripción                                            | Rol Req.          | Código HTTP  |
|---------|------------------------------------------------------|--------------------------------------------------------|-------------------|--------------|
| `POST`  | `/api/v1/empresas`                                   | Registrar una nueva empresa y sucursal matriz          | `SUPER_ADMIN`     | `201 Created`|
| `GET`   | `/api/v1/empresas`                                   | Listar todas las empresas de la plataforma             | `SUPER_ADMIN`     | `200 OK`     |
| `PATCH` | `/api/v1/empresas/{id}/estado`                       | Suspender, dar de baja o reactivar empresa             | `SUPER_ADMIN`     | `200 OK`     |
| `GET`   | `/api/v1/empresas/{id}`                              | Consultar empresa por UUID                             | `SUPER_ADMIN`     | `200 OK`     |
| `GET`   | `/api/v1/empresas/ruc/{ruc}`                         | Consultar empresa por RUC                              | `SUPER_ADMIN`     | `200 OK`     |
| `POST`  | `/api/v1/empresas/{id}/sucursales`                   | Agregar una sucursal a la empresa                      | `EMPRESA_ADMIN`   | `201 Created`|
| `GET`   | `/api/v1/empresas/{id}/sucursales`                   | Listar sucursales de la empresa (MT-02)                | `EMPRESA_ADMIN`   | `200 OK`     |
| `PATCH` | `/api/v1/empresas/{id}/sucursales/{sucursalId}/estado` | Toggle ACTIVA↔INACTIVA de sucursal (SUC-04, MT-02) | `EMPRESA_ADMIN`   | `200 OK`     |

---

## 5. COBERTURA Y PRUEBAS

- **Dominio:** `EmpresaTest.java` (10 tests passing).
- **Aplicación:** `EmpresaApplicationServiceTest.java` (8 tests), `ObtenerSucursalesPorEmpresaServiceTest.java` (4 tests), `CambiarEstadoSucursalServiceTest.java` (7 tests).
- **Infraestructura Web:** `EmpresaControllerTest.java` (9 tests passing con RFC 7807).
- **Infraestructura Persistencia:** `EmpresaJpaAdapterTest.java` (5 tests passing con mocks).
- **Suite global del sistema:** 151/151 tests passing (0 fallos) — incluye 11 tests nuevos v1.1.


---

## 1. IDENTIDAD DEL MÓDULO

| Campo              | Valor                                                  |
|--------------------|--------------------------------------------------------|
| **Group ID**       | `com.SITFAI_CORE_ERP_TIENDA`                           |
| **Artifact ID**    | `Api_Tienda` (Bounded Context `core_empresa`)          |
| **Package raíz**   | `com.SITFAI_CORE_ERP_TIENDA.core_empresa`              |
| **Puerto Asignado**| `8081`                                                 |
| **Java**           | `25` (Records inmutables, Domain-Driven Design)        |
| **Arquitectura**   | Hexagonal (Clean Architecture / MCP-01)                |
| **BD & Migración** | MySQL 8.0+ / InnoDB / `V5__init_core_empresa_schema.sql`|

---

## 2. OBJETIVO DEL BOUNDED CONTEXT

El módulo `core-empresa` representa el **Tenant Raíz** del SaaS SITFAI ERP. Todos los datos, bodegas, cajas, pedidos y facturas de la plataforma pertenecen a una `Empresa`.

### Invariantes y Reglas Clave Implementadas:
- **EMP-01:** La Empresa es la entidad raíz; es el discriminador obligatorio (`EmpresaId`) en todo el sistema.
- **EMP-02:** Toda Empresa tiene un RUC único e inmutable de 11 dígitos numéricos (`Ruc`).
- **EMP-03:** Una Empresa debe contar con al menos una Sucursal activa para operar (`tieneSucursalActiva()`).
- **EMP-04:** Ciclo de vida: `ACTIVA`, `SUSPENDIDA`, `BAJA`.
- **EMP-06:** Una Empresa suspendida no puede añadir sucursales ni activar operaciones.
- **EMP-07:** La transición a `BAJA` es definitiva y desactiva en cascada todas sus sucursales.
- **SUC-01 / SUC-02:** Las sucursales pertenecen a una Empresa y su código local es único dentro de la misma (case-insensitive).
- **SUC-04 / SUC-06:** Control de estado de Sucursal (`ACTIVA`, `INACTIVA`).

---

## 3. ESTRUCTURA COMPLETA DE PAQUETES (MCP-01)

```text
com.SITFAI_CORE_ERP_TIENDA.core_empresa
├── domain/
│   ├── exception/
│   │   ├── DomainException.java
│   │   ├── EmpresaInvalidaException.java
│   │   ├── EmpresaNoEncontradaException.java
│   │   └── RucInvalidoException.java
│   ├── valueobject/
│   │   ├── EmpresaId.java
│   │   ├── SucursalId.java
│   │   ├── Ruc.java
│   │   └── NombreEmpresa.java
│   ├── model/
│   │   ├── EstadoEmpresa.java
│   │   ├── EstadoSucursal.java
│   │   ├── Sucursal.java
│   │   └── Empresa.java
│   └── event/
│       ├── DomainEvent.java
│       ├── EmpresaCreadaEvent.java
│       ├── EmpresaSuspendidaEvent.java
│       ├── EmpresaReactivadaEvent.java
│       ├── EmpresaDadaDeBajaEvent.java
│       ├── SucursalAgregadaEvent.java
│       ├── SucursalDesactivadaEvent.java
│       └── SucursalActivadaEvent.java
│
├── application/
│   ├── dto/
│   │   ├── RegistrarEmpresaCommand.java
│   │   ├── AgregarSucursalCommand.java
│   │   ├── SuspenderEmpresaCommand.java
│   │   ├── DarDeBajaEmpresaCommand.java
│   │   ├── EmpresaResponse.java
│   │   └── SucursalResponse.java
│   ├── mapper/
│   │   └── EmpresaApplicationMapper.java
│   ├── port/
│   │   ├── input/
│   │   │   ├── RegistrarEmpresaUseCase.java
│   │   │   ├── AgregarSucursalUseCase.java
│   │   │   ├── CambiarEstadoEmpresaUseCase.java
│   │   │   └── ConsultarEmpresaUseCase.java
│   │   └── output/
│   │       ├── EmpresaRepository.java
│   │       └── EmpresaEventPublisher.java
│   └── service/
│       ├── RegistrarEmpresaService.java
│       ├── AgregarSucursalService.java
│       ├── CambiarEstadoEmpresaService.java
│       └── ConsultarEmpresaService.java
│
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── web/
    │   │       ├── dto/
    │   │       │   ├── RegistrarEmpresaRequest.java
    │   │       │   ├── AgregarSucursalRequest.java
    │   │       │   ├── CambiarEstadoEmpresaRequest.java
    │   │       │   ├── EmpresaWebResponse.java
    │   │       │   └── SucursalWebResponse.java
    │   │       ├── mapper/
    │   │       │   └── EmpresaWebMapper.java
    │   │       ├── EmpresaController.java
    │   │       └── CoreEmpresaExceptionHandler.java
    │   └── out/
    │       ├── persistence/
    │       │   ├── entity/
    │       │   │   ├── EmpresaJpaEntity.java
    │       │   │   └── SucursalJpaEntity.java
    │       │   ├── repository/
    │       │   │   └── EmpresaJpaRepository.java
    │       │   ├── mapper/
    │       │   │   └── EmpresaPersistenceMapper.java
    │       │   └── EmpresaJpaAdapter.java
    │       └── event/
    │           └── SpringEventEmpresaPublisher.java
```

---

## 4. ENDPOINTS REST (PUERTO 8081 / BASE: `/api/v1/empresas`)

| Método  | Endpoint                   | Descripción                                     | Código HTTP |
|---------|----------------------------|-------------------------------------------------|-------------|
| `POST`  | `/api/v1/empresas`          | Registrar una nueva empresa y sucursal matriz   | `201 Created` |
| `POST`  | `/api/v1/empresas/{id}/sucursales` | Agregar una sucursal a la empresa        | `201 Created` |
| `PATCH` | `/api/v1/empresas/{id}/estado` | Suspender, dar de baja o reactivar empresa   | `200 OK`    |
| `GET`   | `/api/v1/empresas/{id}`     | Consultar empresa por UUID                      | `200 OK`    |
| `GET`   | `/api/v1/empresas/ruc/{ruc}`| Consultar empresa por RUC                       | `200 OK`    |
| `GET`   | `/api/v1/empresas`          | Listar todas las empresas                       | `200 OK`    |

---

## 5. COBERTURA Y PRUEBAS

- **Dominio:** `EmpresaTest.java` (10 tests passing).
- **Aplicación:** `EmpresaApplicationServiceTest.java` (8 tests passing con Mockito).
- **Infraestructura Web:** `EmpresaControllerTest.java` (9 tests passing con RFC 7807).
- **Infraestructura Persistencia:** `EmpresaJpaAdapterTest.java` (5 tests passing con mocks).
- **Suite global del sistema:** 124/124 tests passing (0 fallos).
