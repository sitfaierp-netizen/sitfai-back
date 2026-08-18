# SITFAI ERP — Reglas de Negocio Globales y Multitenancy
> **Versión:** 1.0.0 | **Fecha:** 2026-08-05 | **Estado:** ACTIVO

---

## MODELO JERÁRQUICO DEL SISTEMA

```
SITFAI ERP (SaaS Platform)
└── Empresa (Tenant Raíz)
    ├── Sucursal 1
    │   ├── Bodega A
    │   │   └── Productos / Stock
    │   └── Caja 1
    │       └── Turno de Caja / Transacciones
    └── Sucursal 2
        ├── Bodega B
        └── Caja 2
```

---

## REGLAS DE NEGOCIO: EMPRESA (Tenant Raíz)

| ID     | Regla                                                                             |
|--------|-----------------------------------------------------------------------------------|
| EMP-01 | Una Empresa es la entidad raíz del sistema; todos los datos le pertenecen.        |
| EMP-02 | Toda Empresa tiene un RUC único e irrepetible a nivel de plataforma.              |
| EMP-03 | Una Empresa debe tener al menos una Sucursal activa para operar.                  |
| EMP-04 | Una Empresa puede estar en estado: ACTIVA, SUSPENDIDA, BAJA.                      |
| EMP-05 | Solo el SUPER_ADMIN puede crear, suspender o dar de baja Empresas.                |
| EMP-06 | La Empresa SUSPENDIDA no puede generar nuevas transacciones operativas.            |
| EMP-07 | Los datos de una Empresa BAJA se retienen por el período legal (definible).       |

---

## REGLAS DE NEGOCIO: SUCURSAL

| ID     | Regla                                                                             |
|--------|-----------------------------------------------------------------------------------|
| SUC-01 | Una Sucursal pertenece a exactamente una Empresa (empresa_id es obligatorio).     |
| SUC-02 | El código de Sucursal es único dentro de la Empresa (no globalmente).             |
| SUC-03 | Una Sucursal puede tener múltiples Bodegas y múltiples Cajas.                     |
| SUC-04 | Una Sucursal puede estar: ACTIVA, INACTIVA.                                       |
| SUC-05 | Solo el EMPRESA_ADMIN puede crear o desactivar Sucursales de su Empresa.          |
| SUC-06 | Una Sucursal INACTIVA no puede recibir operaciones de Caja ni de Bodega.          |

---

## REGLAS DE NEGOCIO: BODEGA

| ID     | Regla                                                                             |
|--------|-----------------------------------------------------------------------------------|
| BOD-01 | Una Bodega pertenece a exactamente una Sucursal.                                  |
| BOD-02 | El código de Bodega es único dentro de la Sucursal.                               |
| BOD-03 | Una Bodega es el único lugar donde se registran los movimientos de stock.         |
| BOD-04 | No existe movimiento de stock sin un documento fuente (Orden de Compra, etc.).    |
| BOD-05 | El stock en una Bodega nunca puede ser negativo (regla de dominio invariante).    |
| BOD-06 | Las transferencias entre Bodegas generan dos movimientos: salida y entrada.       |
| BOD-07 | Solo BODEGA_OPERATOR y roles superiores pueden registrar movimientos de stock.    |
| BOD-08 | El sistema emitirá una alerta automática cuando el stock de un producto alcance o caiga por debajo de su punto de reorden definido. |

---

## REGLAS DE NEGOCIO: CAJA

| ID     | Regla                                                                             |
|--------|-----------------------------------------------------------------------------------|
| CAJ-01 | Una Caja pertenece a exactamente una Sucursal.                                    |
| CAJ-02 | Solo puede haber un Turno (Sesión de Caja) abierto por Caja a la vez.            |
| CAJ-03 | Un Turno debe ser abierto con un monto inicial declarado (apertura).              |
| CAJ-04 | Un Turno no puede cerrarse si hay transacciones pendientes de conciliar.          |
| CAJ-05 | El cierre de Turno genera un Arqueo de Caja inmutable (no editable post-cierre). |
| CAJ-06 | Solo el CAJERO asignado puede operar la Caja; el SUCURSAL_MANAGER puede cerrar.  |
| CAJ-07 | Toda transacción de Caja tiene tipo: VENTA, DEVOLUCION, INGRESO, EGRESO.          |
| CAJ-08 | Las devoluciones revierten el stock en la Bodega de origen de la venta.           |

---

## REGLAS DE NEGOCIO: MULTITENANCY (TRANSVERSALES)

| ID      | Regla                                                                             |
|---------|-----------------------------------------------------------------------------------|
| MT-01   | El `empresa_id` está presente en TODOS los registros operacionales de la BD.      |
| MT-02   | Ningún endpoint puede retornar datos de otra Empresa que la del token JWT.        |
| MT-03   | Las migraciones de BD incluyen siempre el `empresa_id` en la clave de RLS.       |
| MT-04   | Los reportes y exportaciones están siempre acotados al tenant del usuario.        |
| MT-05   | No existe compartir recursos (productos, clientes) entre Empresas distintas.      |
| MT-06   | El `empresa_id` en el JWT es el `empresa_id` en Keycloak user attributes.         |

---

## REGLAS DE NEGOCIO: AUDITORÍA (TRANSVERSALES)

| ID      | Regla                                                                             |
|---------|-----------------------------------------------------------------------------------|
| AUD-01  | Todo agregado de negocio tiene: `created_at`, `updated_at`, `created_by`.         |
| AUD-02  | Las operaciones de baja son **lógicas** (soft delete): campo `deleted_at`.        |
| AUD-03  | Los Eventos de Dominio se persisten en una tabla de `domain_events` (Event Store).|
| AUD-04  | Ningún registro financiero (transacción, arqueo) puede ser eliminado físicamente. |
| AUD-05  | Las modificaciones de precios, costos o tarifas se versionan (no se sobreescriben)|

---

## LENGUAJE UBICUO GLOBAL (Ubiquitous Language)

| Término              | Definición en el Dominio SITFAI                                    |
|----------------------|--------------------------------------------------------------------|
| **Empresa**          | Tenant raíz. Entidad legal que contrata el ERP.                   |
| **Sucursal**         | Punto de operación físico o virtual de la Empresa.                |
| **Bodega**           | Espacio de almacenamiento de stock dentro de una Sucursal.        |
| **Caja**             | Punto de transacción económica dentro de una Sucursal.            |
| **Turno**            | Sesión de operación de una Caja (apertura → cierre).              |
| **Arqueo**           | Resumen financiero inmutable al cierre de Turno.                  |
| **Movimiento**       | Registro de entrada o salida de stock en una Bodega.              |
| **Documento Fuente** | Documento que justifica un movimiento (Orden, Factura, etc.).     |
| **Tenant**           | Sinónimo técnico de Empresa en contexto de multitenancy.          |
| **Realm**            | Dominio de Keycloak que agrupa a todos los tenants del ERP.       |
| **Rol**              | Permiso asignado a un usuario por su posición en la jerarquía.    |
| **Invariante**       | Regla de dominio que NUNCA puede violarse, sin excepciones.       |

---

## MONEDA Y DECIMALES

| Regla                                                                              |
|------------------------------------------------------------------------------------|
| El sistema soporta múltiples monedas por Empresa (configuración por Empresa).     |
| Los montos monetarios se almacenan como `NUMERIC(19, 4)` en PostgreSQL.           |
| Las operaciones de redondeo usan `HALF_UP` (estándar contable).                   |
| La moneda base de la Empresa se define al momento de la creación (inmutable).     |

---

## REGLAS DE NUMERACIÓN DE DOCUMENTOS

| Regla                                                                              |
|------------------------------------------------------------------------------------|
| Los números de documento (Facturas, OC, etc.) son únicos por Empresa.             |
| Los prefijos de numeración se configuran por Sucursal o por Empresa.              |
| La secuencia de numeración es gestionada por el módulo que emite el documento.    |
| Los números asignados son inmutables (no se pueden reasignar ni reciclar).        |
