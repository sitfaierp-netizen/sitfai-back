# SITFAI ERP - Manual de Pruebas End-to-End (E2E)
> **Estado:** Oficial | **Módulo:** Core ERP

## 1. Jerarquía de Roles
El flujo del sistema se divide estrictamente según los permisos otorgados por Keycloak:
- **SUPER_ADMIN**: Rol supremo. Gestiona la infraestructura, crea Empresas (Tenants Raíz).
- **EMPRESA_ADMIN**: Dueño/Gerente de un Tenant. Crea Sucursales, administra personal y registra la Data Maestra base.
- **COMPRAS_MANAGER**: Encargado de compras y proveedores. Aprueba solicitudes y emite órdenes de compra.
- **SUCURSAL_MANAGER**: Administrador de una sucursal específica. Encargado de cierres de caja y supervisión.
- **BODEGA_OPERATOR**: Encargado de almacenes. Único autorizado para solicitar abastecimiento y registrar movimientos de stock.
- **CAJERO**: Operador de Punto de Venta (POS). Abre turnos y registra transacciones financieras diarias.

---

## 2. Fases del Flujo E2E

### Fase 1: Fundación Legal (Infraestructura Base)
**Actor:** SUPER_ADMIN
**Objetivo:** Crear el espacio aislado (Tenant) para un nuevo cliente.
- **Llamada REST:** POST `/empresas`
- **Dependencias:** Ninguna. 
- **Retorno Clave:** `empresaId` (Requerido para todo el resto del flujo MT-01).

### Fase 2: Infraestructura y RRHH
**Actor:** EMPRESA_ADMIN
**Objetivo:** Configurar las ubicaciones físicas y el personal del cliente.
- **Llamadas REST:**
  - POST `/empresas/{empresaId}/sucursales` -> Retorna `sucursalId`.
  - POST `/bodegas` -> Con `sucursalId`. Retorna `bodegaId`.
  - POST `/iam/usuarios` -> Crea usuarios con roles: `COMPRAS_MANAGER`, `BODEGA_OPERATOR`, `CAJERO`.

### Fase 3: Data Maestra (¡NUEVO!)
**Actor:** EMPRESA_ADMIN / COMPRAS_MANAGER
**Objetivo:** Registrar el catálogo de productos y los proveedores oficiales, creando el Source of Truth.
- **Llamadas REST:**
  - POST `/catalog/categorias` -> (Actor: EMPRESA_ADMIN) Retorna `categoriaId`.
  - POST `/catalog/productos` -> (Actor: EMPRESA_ADMIN) Usa `categoriaId`. Retorna `productoId`.
  - POST `/sourcing/proveedores` -> (Actor: EMPRESA_ADMIN o COMPRAS_MANAGER) Retorna `proveedorId`.

### Fase 4: Solicitud Operativa (¡NUEVO!)
**Actor:** BODEGA_OPERATOR
**Objetivo:** Solicitar reabastecimiento de stock para la bodega usando productos oficiales del catálogo.
- **Llamada REST:** POST `/api/v1/purchasing/solicitudes`
  - **Dependencias:** Requiere `bodegaId` (Fase 2) y los `productoId` reales del catálogo (Fase 3).
  - **Retorno Clave:** `solicitudId`.

### Fase 5: Abastecimiento y Logística
**Actores:** COMPRAS_MANAGER y BODEGA_OPERATOR
**Objetivo:** Aprobar la solicitud, generar la orden de compra y dar ingreso al stock físico.
- **Llamadas REST:**
  - PATCH `/api/v1/purchasing/solicitudes/{id}/aprobar` -> (Actor: COMPRAS_MANAGER)
  - POST `/api/v1/purchasing/ordenes` -> (Actor: COMPRAS_MANAGER) Genera OC usando la Solicitud aprobada y el `proveedorId` (Fase 3).
  - POST `/bodegas/{bodegaId}/movimientos` -> (Actor: BODEGA_OPERATOR) Registra la entrada (IN) de stock referenciando la OC generada.

### Fase 6: Operación de Ventas (POS & Billing)
**Actor:** CAJERO (Apertura/Venta) y SUCURSAL_MANAGER (Cierre)
**Objetivo:** Abrir el punto de venta, vender usando los productos del catálogo y generar el arqueo.
- **Llamadas REST:**
  - POST `/pos/turnos` -> (Actor: CAJERO) Abre turno con monto inicial. Retorna `turnoId`.
  - POST `/pos/turnos/{turnoId}/transacciones` -> (Actor: CAJERO) Registra ventas asociadas al turno usando los `productoId` reales del catálogo (Fase 3).
  - PATCH `/pos/turnos/{turnoId}/cerrar` -> (Actor: SUCURSAL_MANAGER o CAJERO) Clausura y congela el arqueo de forma inmutable.
