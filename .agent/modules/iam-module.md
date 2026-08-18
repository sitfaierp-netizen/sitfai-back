# SITFAI ERP — IAM Module (com.SITFAI_CORE_ERP_TIENDA.iam_module)
> **Versión:** 1.0.0 | **Fecha:** 2026-08-07 | **Estado:** 🟢 COMPLETADO Y VALIDADO

---

## 1. IDENTIDAD DEL MÓDULO

| Campo              | Valor                                                              |
|--------------------|--------------------------------------------------------------------|
| **Group ID**       | `com.SITFAI_CORE_ERP_TIENDA`                                       |
| **Artifact ID**    | `Api_Tienda` (Bounded Context `iam_module`)                        |
| **Package raíz**   | `com.SITFAI_CORE_ERP_TIENDA.iam_module`                            |
| **Puerto Asignado**| `8082`                                                             |
| **Java**           | `25` (Records inmutables, Domain-Driven Design)                    |
| **Arquitectura**   | Hexagonal (Clean Architecture / MCP-01 / Protocolo 4 Cero Passwords) |

---

## 2. OBJETIVO DEL BOUNDED CONTEXT

El módulo `iam-module` (Identity & Access Management) gestiona la identidad, roles y ciclo de vida de los usuarios/empleados de cada tenant (Empresa) del sistema SaaS SITFAI ERP. Las credenciales de autenticación son gestionadas de forma perimetral por Keycloak (Protocolo 4).

### Invariantes y Reglas de Negocio:
- **MT-01:** Todo Usuario pertenece a un Tenant Raíz (`EmpresaId`). En cada consulta, persistencia y caso de uso, el `EmpresaId` es obligatorio.
- **Protocolo 4 (Cero Contraseñas en BD):** La entidad de usuario no persiste contraseñas; Keycloak maneja los passwords, tokens y SSO.
- **MT-06 (Sincronización y Roles IAM):** Los usuarios registrados son sincronizados automáticamente con Keycloak, inyectando el atributo `empresa_id` y asignando el rol de Realm correspondiente (`SUPER_ADMIN`, `EMPRESA_ADMIN`, `SUCURSAL_MANAGER`, `BODEGA_OPERATOR`, `CAJERO`).
- **Invariante Username:** Alfanumérico, mínimo 4 caracteres, máximo 50. Único por empresa (`UQ(empresa_id, username)`).
- **Invariante Email:** RFC 5322 normalizado a minúsculas. Único por empresa (`UQ(empresa_id, email)`).
- **Ciclo de Vida:** Nace `ACTIVO`. Puede ser `desactivar()` o `reactivar()`. Un usuario inactivo no puede modificar su rol ni operar.
- **Eventos de Dominio:** Emisión de `UsuarioRegistradoEvent`, `UsuarioDesactivadoEvent`, `UsuarioReactivadoEvent` y `RolUsuarioModificadoEvent`.

---

## 3. INTEGRACIÓN DE IDENTIDAD CON KEYCLOAK

### Adaptadores Implementados:
1. **`KeycloakClientConfig.java`:** Configuración del cliente administrador `org.keycloak.admin.client.Keycloak` inyectando propiedades de entorno (`keycloak.admin.server-url`, `keycloak.admin.realm`, etc.).
2. **`KeycloakUsuarioSyncListener.java`:** Driven Adapter que escucha el evento `UsuarioRegistradoEvent` y realiza:
   - Creación de `UserRepresentation` con `username`, `email` y credencial temporal (`temporary = true`).
   - Inyección del atributo multitenant `empresa_id` según **MT-06**.
   - Asignación atómica del rol de Realm correspondiente.

---

## 4. ESTRUCTURA COMPLETA DE CAPAS (MCP-01)

```text
com.SITFAI_CORE_ERP_TIENDA.iam_module/
├── domain/
│   ├── exception/
│   │   ├── DomainException.java
│   │   ├── UsuarioInvalidoException.java
│   │   └── UsuarioNoEncontradoException.java
│   ├── valueobject/
│   │   ├── UsuarioId.java
│   │   ├── EmpresaId.java
│   │   ├── Username.java
│   │   └── Email.java
│   ├── model/
│   │   ├── RolUsuario.java
│   │   ├── EstadoUsuario.java
│   │   └── Usuario.java (Aggregate Root)
│   └── event/
│       ├── DomainEvent.java
│       ├── UsuarioRegistradoEvent.java
│       ├── UsuarioDesactivadoEvent.java
│       ├── UsuarioReactivadoEvent.java
│       └── RolUsuarioModificadoEvent.java
├── application/
│   ├── dto/
│   │   ├── RegistrarUsuarioCommand.java
│   │   ├── DesactivarUsuarioCommand.java
│   │   ├── ReactivarUsuarioCommand.java
│   │   ├── CambiarRolUsuarioCommand.java
│   │   └── UsuarioResponse.java
│   ├── mapper/
│   │   └── UsuarioApplicationMapper.java
│   ├── port/
│   │   ├── input/
│   │   │   ├── RegistrarUsuarioUseCase.java
│   │   │   ├── DesactivarUsuarioUseCase.java
│   │   │   ├── ReactivarUsuarioUseCase.java
│   │   │   ├── CambiarRolUsuarioUseCase.java
│   │   │   └── ConsultarUsuarioUseCase.java
│   │   └── output/
│   │       ├── UsuarioRepository.java (MT-01 enforce)
│   │       └── UsuarioEventPublisher.java
│   └── service/
│       ├── RegistrarUsuarioService.java
│       ├── GestionarEstadoUsuarioService.java
│       ├── CambiarRolUsuarioService.java
│       └── ConsultarUsuarioService.java
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   └── web/
    │   │       ├── dto/
    │   │       │   ├── RegistrarUsuarioRequest.java
    │   │       │   ├── DesactivarUsuarioRequest.java
    │   │       │   ├── ReactivarUsuarioRequest.java
    │   │       │   └── CambiarRolUsuarioRequest.java
    │   │       ├── UsuarioWebMapper.java
    │   │       ├── UsuarioController.java (/api/v1/iam/usuarios)
    │   │       └── IamExceptionHandler.java (RFC 7807)
    │   └── out/
    │       ├── persistence/
    │       │   ├── UsuarioJpaEntity.java
    │       │   ├── UsuarioJpaRepository.java
    │       │   ├── UsuarioPersistenceMapper.java
    │       │   └── UsuarioJpaAdapter.java
    │       ├── event/
    │       │   └── SpringEventUsuarioPublisher.java
    │       └── identity/
    │           ├── KeycloakClientConfig.java
    │           └── KeycloakUsuarioSyncListener.java (MT-06)
    └── ...
```

---

## 5. ESQUEMA DE BASE DE DATOS (FLYWAY V6)

Archivo: `src/main/resources/db/migration/V6__init_iam_schema.sql`

```sql
CREATE TABLE IF NOT EXISTS iam_usuario (
    id VARCHAR(36) NOT NULL,
    empresa_id VARCHAR(36) NOT NULL,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(150) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    estado VARCHAR(20) NOT NULL,
    creado_en TIMESTAMP WITH TIME ZONE NOT NULL,
    actualizado_en TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT pk_iam_usuario PRIMARY KEY (id),
    CONSTRAINT uq_iam_usuario_empresa_username UNIQUE (empresa_id, username),
    CONSTRAINT uq_iam_usuario_empresa_email UNIQUE (empresa_id, email)
);

CREATE INDEX IF NOT EXISTS idx_iam_usuario_empresa_id ON iam_usuario (empresa_id);
CREATE INDEX IF NOT EXISTS idx_iam_usuario_empresa_username ON iam_usuario (empresa_id, username);
CREATE INDEX IF NOT EXISTS idx_iam_usuario_empresa_email ON iam_usuario (empresa_id, email);
```

---

## 6. ESTADO DE PRUEBAS AUTOMATIZADAS

| Capa / Componente | Archivo de Prueba | Estado | Tests |
|---|---|---|---|
| Dominio | `UsuarioTest.java` | ✅ APROBADO | 36 |
| Aplicación | `UsuarioApplicationServiceTest.java` | ✅ APROBADO | 10 |
| Infraestructura Web | `UsuarioControllerTest.java` | ✅ APROBADO | 8 |
| Infraestructura Persistencia | `UsuarioJpaAdapterTest.java` | ✅ APROBADO | 5 |
| Infraestructura Seguridad (Keycloak) | `KeycloakUsuarioSyncListenerTest.java` | ✅ APROBADO | 2 |
| Integración Testcontainers | `UsuarioJpaRepositoryIT.java` | ✅ CONFIGURADO | — |
| **Total General Suite** | `mvn test` | **✅ EXITOSO** | **185 tests pasados (0 fallos)** |
