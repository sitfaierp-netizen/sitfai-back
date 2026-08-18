# Módulo: billing — Facturación Electrónica DIAN
> **Estado:** 🟢 EN DESARROLLO (Aplicación - Notas de Crédito) | **Fecha:** 2026-08-11 | **Revisión:** 1.4.0

---

## Bounded Context

**Nombre:** `billing`
**Paquete raíz:** `com.SITFAI_CORE_ERP_TIENDA.billing`
**Responsabilidad:** Motor tributario de Colombia (Facturación Electrónica DIAN). Emisión, firma, y validación de comprobantes fiscales electrónicos.

---

## Lenguaje Ubicuo (Ubiquitous Language - DIAN)

Para asegurar la correcta alineación con la normatividad tributaria colombiana (DIAN), este módulo utiliza un lenguaje propio y altamente restrictivo:

| Término | Definición en el Dominio DIAN |
|---------|--------------------------------|
| **NIT** | Número de Identificación Tributaria (Emisor o Receptor). Reemplaza al RUC genérico. |
| **CUFE** | Código Único de Facturación Electrónica (sha384). Huella criptográfica inmutable. |
| **Resolución DIAN** | Autorización oficial que define un prefijo, un rango de números y una vigencia para facturar. |
| **Impuesto** | Tributo aplicado a un concepto. En Colombia incluye IVA, RETEFUENTE, ICA, entre otros. |
| **Línea de Factura** | Detalle unitario que contiene concepto, cantidad, precio e impuestos específicos aplicables. |
| **Factura Electrónica**| Documento fiscal legal con validez ante la DIAN. Agregado principal del módulo. |

---

## Reglas de Negocio Implementadas (Dominio)

| ID     | Regla                                                              | Implementación                                    |
|--------|--------------------------------------------------------------------|---------------------------------------------------|
| MT-01  | `empresa_id` presente en todos los registros                      | `EmpresaId empresaId` en `FacturaElectronica`     |
| DIAN-1 | Una Factura Electrónica necesita un NIT emisor y un NIT receptor  | Value Object `Nit` con validación de formato      |
| DIAN-2 | La firma de la Factura exige un CUFE                              | `FacturaElectronica.firmar(Cufe)`                 |
| DIAN-3 | No se puede facturar con una Resolución expirada                  | `ResolucionDian.estaExpirada()` en método `firmar`|
| DIAN-4 | Cálculos financieros con precisión estricta (cero floats)         | Value Object `Dinero` con `BigDecimal`            |
| DIAN-5 | No se puede firmar una factura sin detalle (líneas)               | Invariante en `FacturaElectronica.firmar()`       |
| DIAN-6 | Nota de Crédito exige CUFE y Factura original (No huérfana)       | Factory de `NotaCreditoElectronica`               |

---

## Diseño del Modelo (Clean Architecture - Capa de Dominio Pura)

Este módulo fue diseñado con CERO dependencias a Spring, JPA, y otros frameworks técnicos, basándose exclusivamente en records y clases de Java 21 LTS puro, garantizando que toda la matemática impositiva pueda ser testeada unitariamente con precisión.

- **Value Objects:** `Cufe`, `Nit`, `ResolucionDian`, `Dinero`, `Impuesto`, `NotaCreditoId`, `MotivoDevolucion`.
- **Entities:** `LineaFactura`.
- **Aggregate Roots:** 
  - `FacturaElectronica`.
  - `NotaCreditoElectronica` (Logística Inversa).
- **Events:** `FacturaFirmadaEvent`, `NotaCreditoFirmadaEvent`.

---

## Orquestación y Aplicación

- **Emisión de Factura:** `EmitirFacturaService` orquesta la inyección de `EmpresaId`, la obtención de la resolución DIAN y los comandos.
- **Emisión de Nota de Crédito (Reverso):** `EmitirNotaCreditoService` protege contra notas huérfanas validando la pre-existencia y firma (CUFE) de la `FacturaElectronica` original mediante `FacturaRepository`, cumpliendo estrictamente con las reglas DIAN.

---

## Implementación de Infraestructura y Base de Datos

- **Controllers REST:** `FacturaController` protegido con `@PreAuthorize` y extracción obligatoria de `empresa_id` desde el JWT (Zero Trust).
- **Manejo de Errores:** Implementado `BillingExceptionHandler` bajo el estándar RFC 7807 (Problem Details).
- **Persistencia JPA:** `FacturaJpaEntity` y `LineaFacturaJpaEntity` completamente desvinculadas del dominio. Se utiliza `FacturaPersistenceMapper` como traductor.
- **Base de Datos (Flyway V18):**
  - Tipado matemático estricto: `DECIMAL(19, 4)`.
  - UUIDs forzados a `VARCHAR(36)` para consistencia en cruces.
  - Índices compuestos con `empresa_id` por diseño MT-01.

---

## Coreografía Asíncrona (Messaging)

El módulo se integra asíncronamente con otros Bounded Contexts para automatizar la facturación sin acoplamiento duro (Regla 1 y 5):

1. **Consumo de `VentaRegistradaEvent` (POS / E-commerce):**
   - **Listener:** `VentaRealizadaBillingListener` (@Async @EventListener).
   - **Mapeo y Transformación:** Traduce las líneas de venta a comandos de facturación, inyectando impuestos base (ej. IVA 19%).
   - **Fallback Legal (DIAN):** Si el evento original no declara un cliente explícito, el listener asigna automáticamente el NIT genérico `222222222222` ("Consumidor Final") para garantizar que la emisión nunca falle por falta de receptor, aislando la regla tributaria del POS.
