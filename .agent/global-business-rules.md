# SITFAI ERP — Catálogo Global de Reglas de Negocio (V1.0)
> **Versión:** 1.0.0 | **Fecha:** 2026-08-19 | **Estado:** CONGELADO PARA AUDITORÍA

Este documento es la única fuente de la verdad para las invariantes y reglas de negocio del ecosistema SITFAI ERP (Java 21, Spring Boot 3.4.0 LTS). Toda nueva funcionalidad en `purchasing`, `orders`, `billing` o módulos futuros debe adherirse estrictamente a estas reglas.

---

## 1. Reglas de Multi-Tenancy e Identidad (MT / IAM)

| ID     | Definición de la Regla                                                                                 |
|--------|--------------------------------------------------------------------------------------------------------|
| **MT-01**  | **Aislamiento Físico/Lógico:** El campo `empresa_id` debe estar presente en TODOS los registros transaccionales y maestros como clave de partición obligatoria. |
| **MT-02**  | **Prohibición de Payload:** Ningún Request DTO ni Endpoint REST debe aceptar el `empresa_id` como parámetro del cliente. Siempre se extrae delegando a Keycloak (vía claims del JWT). |
| **MT-03**  | **Unicidad Multi-Tenant:** Las restricciones `UNIQUE` en base de datos deben ser siempre compuestas, anteponiendo `empresa_id` (Ej. `empresa_id + ruc`). |
| **IAM-01** | **Delegación Jerárquica:** El usuario autenticado en Keycloak solo tiene acceso al `empresa_id` asignado a su atributo. Las contraseñas jamás se almacenan en PostgreSQL. |

---

## 2. Reglas de Maestros (EMP / CAT / SRC)

| ID     | Definición de la Regla                                                                                 |
|--------|--------------------------------------------------------------------------------------------------------|
| **EMP-01** | **RUC Único:** Toda Empresa tiene un RUC fiscal válido, único e irrepetible a nivel global del SaaS.   |
| **CAT-01** | **Unicidad de Producto:** El código SKU de un Producto debe ser único dentro de la misma Empresa (`empresa_id + sku`). |
| **CAT-02** | **Manejo de Estados:** Todo maestro de producto/categoría posee estados lógicos (ACTIVO, INACTIVO) para ocultar ítems obsoletos sin eliminarlos. |
| **SRC-01** | **Unicidad de Proveedor:** El RUC de un Proveedor debe ser único dentro de la misma Empresa (`empresa_id + ruc`). |

---

## 3. Reglas Transaccionales y de Compras/Ventas (DOC / PED / PUR)

| ID     | Definición de la Regla                                                                                 |
|--------|--------------------------------------------------------------------------------------------------------|
| **DOC-01** | **Inmutabilidad Documental:** Un documento financiero cerrado o emitido (Factura, OC, Arqueo) no puede sufrir alteraciones. Sus líneas e importes quedan sellados. |
| **DOC-02** | **Unicidad de Secuencia:** La combinación de serie y número de documento debe ser única por Empresa y tipo de documento. |
| **PUR-01** | **Máquina de Estados Estricta:** Las transacciones siguen un flujo unidireccional. Una OC solo pasa a `RECIBIDA` si primero estuvo `ENVIADA`. No se admiten saltos de estado. |

---

## 4. Reglas de Inventario y Control Físico (BOD / INV)

| ID     | Definición de la Regla                                                                                 |
|--------|--------------------------------------------------------------------------------------------------------|
| **BOD-04** | **Obligatoriedad de Documento Fuente:** Ningún movimiento de stock ocurre en el vacío. Toda alteración de stock exige un documento fuente traceable (Compra, Venta, Devolución). |
| **BOD-05** | **Stock No Negativo:** Invariante inquebrantable. El stock de un producto en bodega jamás puede ser menor a cero. |
| **INV-01** | **Consolidación FEFO:** Para productos perecederos, el despacho de lotes debe acatar estricta y algorítmicamente el principio First Expires, First Out. |

---

## 5. Reglas Financieras y Transversales (MONEY / AUD / DEL / IDEMP / CON)

| ID     | Definición de la Regla                                                                                 |
|--------|--------------------------------------------------------------------------------------------------------|
| **MONEY-01**| **Uso Estricto de BigDecimal:** Todo monto monetario se manipula con `BigDecimal` en Java y se persiste con precisión `DECIMAL(19,4)`. Cero uso de `Double` o `Float`. |
| **AUD-01** | **Auditoría de Operaciones Críticas:** Registros transaccionales guardan `created_at` y el usuario (`created_by`) que ejecutó la operación extraído del token IAM. |
| **DEL-01** | **Baja Lógica (Soft-Delete):** Las operaciones de borrado en entidades core son lógicas (campo `deleted_at`). Prohibido el uso de `DELETE FROM` físico en BD. |
| **IDEMP-01**| **Idempotencia:** Toda operación HTTP no segura (`POST`/`PATCH`) sobre agregados financieros debe soportar reintentos seguros mediante llaves de idempotencia. |
| **CON-01** | **Concurrencia Optimista:** Módulos de alto tráfico (ej. Inventario/Ventas) deben protegerse contra dirty-writes usando control de versión de registro (`@Version`). |

---

## MATRIZ DE DEFENSA EN PROFUNDIDAD (AUDITORÍA ACTUAL)

La siguiente matriz asigna la capa de defensa a cada regla y reporta su estado real en el código construido (`core_empresa`, `iam`, `catalog`, `sourcing`, `inventory`):

| Código Regla | Descripción | Dominio | Aplicación | BD (UNIQUE/CHECK) | REST | Estado |
|--------------|-------------|---------|------------|-------------------|------|--------|
| **MT-01**    | Aislamiento inquebrantable | ✅ Validado en Value Objects | ✅ DTOs inyectan valor | ✅ `empresa_id` presente y es FK | ✅ Inyectado desde JWT | ✅ Implementado |
| **MT-02**    | Payload sin `empresa_id` | 🚫 N/A | 🚫 N/A | 🚫 N/A | ✅ `PreAuthorize` y Extracción JWT | ✅ Implementado |
| **IAM-01**   | Delegación en Keycloak | 🚫 N/A | ✅ `TenantProviderPort` | 🚫 N/A | ✅ `SecurityConfig` (LTS 3.4.0) | ✅ Implementado |
| **EMP-01**   | Unicidad RUC Empresa | ✅ `RucInvalidoException` | ✅ Service catch | ✅ `uq_core_empresa_ruc` | ✅ 409 Conflict | ✅ Implementado |
| **CAT-01**   | Unicidad SKU Producto | ✅ Validador de Negocio | ✅ Service catch | ✅ `uq_catalog_prod_empresa_sku` | ✅ 409 Conflict | ✅ Implementado |
| **SRC-01**   | Unicidad RUC Proveedor | ✅ `RucDuplicadoException` | ✅ Service catch | ✅ `uq_sourcing_prov_empresa_ruc` | ✅ 409 Conflict | ✅ Implementado |
| **BOD-04**   | Documento fuente oblig. | ✅ Parámetros en Agregado | ✅ Command requires ID | 🚫 N/A | ✅ `@NotNull` en Request DTO | ✅ Implementado |
| **BOD-05**   | Stock no negativo | ✅ Invariante `BOD-05` | ✅ Service Orquestador | ✅ `chk_inv_bodega_lote_cantidad`| ✅ 422 Unprocessable | ✅ Implementado |
| **INV-01**   | FEFO Consolidado | ✅ Lógica en Agregado | ✅ Domain Service | 🚫 N/A | ✅ Auditoría E2E | ✅ Implementado |
| **MONEY-01** | BigDecimal (19,4) | ✅ VOs numéricos | ✅ DTOs mapeados | ✅ `DECIMAL(19,4)` Flyway | ✅ JSON Deserializer | ✅ Implementado |
| **DOC-01**   | Inmutabilidad Documental | ⏳ Falta protección state | ⏳ Faltan Guards | ⏳ No hay Triggers de DB | ⏳ Endpoints exponen PUT | ⏳ Pendiente |
| **AUD-01**   | Auditoría Op. Críticas | ⏳ Faltan domain events | ⏳ No captura `created_by` | ⏳ Parcial (`created_at`) | 🚫 N/A | ⏳ Pendiente |
| **DEL-01**   | Baja Lógica (Soft-Delete) | ⏳ Falta en Agregados | ⏳ Commands ejecutan DELETE | ⏳ Falta indexación condicional | ⏳ Verbo DELETE expuesto | ⏳ Pendiente |
| **IDEMP-01** | Idempotencia | 🚫 N/A | 🚫 N/A | 🚫 N/A | ⏳ Falta Header e Interceptor | ⏳ Pendiente |
| **CON-01**   | Concurrencia Optimista | ⏳ Falta campo de versión | ⏳ Sin Retry handling | ⏳ Faltan campos `version` SQL | ⏳ No retorna ETags | ⏳ Pendiente |
