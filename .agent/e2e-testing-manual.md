# SITFAI ERP - Manual de Pruebas End-to-End (E2E)
> **Estado:** Oficial | **Módulo:** Core ERP

## 1. Jerarquía de Roles
El flujo del sistema se divide estrictamente según los permisos otorgados por Keycloak:
- **SUPER_ADMIN**: Rol supremo. Gestiona la infraestructura, crea Empresas (Tenants Raíz).
- **EMPRESA_ADMIN**: Dueño/Gerente de un Tenant. Crea Sucursales y administra personal (Usuarios).
- **SUCURSAL_MANAGER**: Administrador de una sucursal específica. Encargado de cierres de caja y supervisión.
- **BODEGA_OPERATOR**: Encargado de almacenes. Único autorizado para registrar movimientos de stock y abastecimiento.
- **CAJERO**: Operador de Punto de Venta (POS). Abre turnos y registra transacciones financieras diarias.

---

## 2. Fases del Flujo E2E

### Fase 1: Fundación Legal (Infraestructura Base)
**Actor:** SUPER_ADMIN
**Objetivo:** Crear el espacio aislado (Tenant) para un nuevo cliente.
- **Llamada REST:** POST /empresas
- **Dependencias:** Ninguna. 
- **Retorno Clave:** empresaId (Requerido para todo el resto del flujo MT-01).

### Fase 2: Infraestructura y RRHH
**Actor:** EMPRESA_ADMIN
**Objetivo:** Configurar las ubicaciones físicas y el personal del cliente.
- **Llamada REST:** POST /empresas/{empresaId}/sucursales
  - **Dependencias:** Requiere el empresaId generado en Fase 1 en el path.
  - **Retorno Clave:** sucursalId.
- **Llamada REST:** POST /iam/usuarios
  - **Dependencias:** Header X-Empresa-Id. Se deben crear usuarios con roles CAJERO y BODEGA_OPERATOR.

### Fase 3: Abastecimiento y Logística
**Actor:** BODEGA_OPERATOR
**Objetivo:** Configurar los almacenes y dar ingreso inicial al inventario.
- **Llamada REST:** POST /bodegas
  - **Dependencias:** En el body debe enviarse el sucursalId (Fase 2).
  - **Retorno Clave:** odegaId.
- **Llamada REST:** POST /bodegas/{bodegaId}/movimientos
  - **Dependencias:** odegaId en el path. Registra la entrada (IN) de stock para los productos.

### Fase 4: Operación de Ventas (POS & Billing)
**Actor:** CAJERO (Apertura/Venta) y SUCURSAL_MANAGER (Cierre)
**Objetivo:** Abrir el punto de venta, vender y generar el arqueo.
- **Llamada REST:** POST /pos/turnos
  - **Actor:** CAJERO
  - **Acción:** Declara la base inicial de dinero en caja.
  - **Retorno Clave:** 	urnoId.
- **Llamada REST:** POST /pos/turnos/{turnoId}/transacciones
  - **Actor:** CAJERO
  - **Acción:** Registra ingresos (ventas) asociadas al turno abierto.
- **Llamada REST:** PATCH /pos/turnos/{turnoId}/cerrar
  - **Actor:** SUCURSAL_MANAGER o CAJERO
  - **Acción:** Clausura la sesión y congela el arqueo de forma inmutable.
