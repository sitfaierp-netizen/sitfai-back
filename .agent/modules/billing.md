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

- **Value Objects:** `Cufe`, `Nit`, `ResolucionDian`, `Dinero`, `Impuesto`, `NotaCreditoId`, `MotivoDevolucion`, `FacturaId`, `ClienteId`, `DocumentoFuenteId` (POS / E-commerce), `EstadoFactura` (EMITIDA, ANULADA), `EmpresaId`.
- **Entities:** `LineaFactura` (protegida, cálculos de subtotal e impuestos fail-fast).
- **Aggregate Roots:** 
  - `FacturaElectronica`.
  - `NotaCreditoElectronica` (Logística Inversa).
  - `Factura` (Facturación unificada multi-origen: POS / E-commerce con invariantes MT-01, cálculo automático de totales y AUD-04 para anulación).
- **Events:** `FacturaFirmadaEvent`, `NotaCreditoFirmadaEvent`, `FacturaEmitidaEvent`, `FacturaAnuladaEvent`.
- **Ports (Output):** `FacturaRepository` (interfaz pura de Java con aislamiento `EmpresaId` en todas sus firmas).

---

## Orquestación y Aplicación

- **Emisión de Factura:** `EmitirFacturaService` orquesta el caso de uso `EmitirFacturaUseCase`. Ejecuta el factory method del Agregado `Factura`, calcula subtotales e impuestos fail-fast, persiste a través de `FacturaRepository` exigiendo `EmpresaId` (MT-01) y publica `FacturaEmitidaEvent` mediante `ApplicationEventPublisher`.
- **Emisión de Nota de Crédito (Reverso):** `EmitirNotaCreditoService` protege contra notas huérfanas validando la pre-existencia y firma (CUFE) de la `FacturaElectronica` original mediante `FacturaRepository`, cumpliendo estrictamente con las reglas DIAN.

---

## Implementación de Infraestructura y Base de Datos

- **Controllers REST:** `FacturaController` protegido con `@PreAuthorize` y extracción obligatoria de `empresa_id` desde el JWT (Zero Trust).
- **Manejo de Errores:** Implementado `BillingExceptionHandler` bajo el estándar RFC 7807 (Problem Details).
- **Persistencia JPA:** `FacturaJpaEntity` y `LineaFacturaJpaEntity` heredan de `AuditableJpaEntity` (AUD-01) garantizando trazabilidad de fechas (`creado_en`, `actualizado_en`) y actores (`creado_por`, `actualizado_por`).
- **Adaptadores:** `FacturaJpaAdapter` implementa el puerto `FacturaRepository` con filtrado mandatorio por `empresa_id` (MT-01). Mapeo limpio con `FacturaPersistenceMapper`.
- **Base de Datos (Flyway V40):**
  - Script `V40__init_billing_schema.sql` crea y ajusta `billing_factura` y `billing_linea_factura`.
  - Precisión matemática estricta: `DECIMAL(19, 4)`.
  - Discriminador multitenant: `empresa_id BINARY(16)`.
  - Trazabilidad documental: `tipo_origen`, `documento_fuente_id` y auditoría de anulación (`motivo_anulacion`, `anulado_en`).

---

## Coreografía Asíncrona (Messaging)

El módulo se integra asíncronamente con otros Bounded Contexts para automatizar la facturación sin acoplamiento duro (Regla 1 y 5):

1. **Consumo de `PedidoConfirmadoEvent` (E-commerce / Api_Tienda):**
   - **Handler:** `PedidoConfirmadoEventHandler` (`billing/infrastructure/adapter/in/messaging`).
   - **Aislamiento Multitenant (MT-01):** Extrae el `empresa_id` directamente del payload del evento (cero dependencia de contextos HTTP).
   - **Mapeo:** Traduce las líneas de pedido a `LineaFacturaCommand` inyectando IVA 19% y ejecuta `EmitirFacturaUseCase`.
2. **Consumo de `VentaRegistradaEvent` (POS):**
   - **Listener:** `VentaRealizadaBillingListener` (@Async @EventListener).
   - **Mapeo y Transformación:** Traduce las líneas de venta a comandos de facturación, inyectando impuestos base.
   - **Fallback Legal (DIAN):** Asigna consumidor final cuando no se especifica cliente.
