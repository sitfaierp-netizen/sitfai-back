# SITFAI ERP — Protocolos Operativos del Agente (MCP)
> **Versión:** 1.1.0 (Refinada para Base Existente) | **Fecha:** 2026-08-10 | **Estado:** 🚀 PRODUCTION-READY

---

## 0. DIRECTRIZ BASE DE PROYECTO (¡CRÍTICO!)
El proyecto `Api_Tienda` **YA ESTÁ INICIALIZADO**.
- **NO** regeneres archivos `pom.xml` desde cero.
- **NO** modifiques las dependencias base a menos que el usuario lo solicite explícitamente.
- **Stack actual:** Java 21 (LTS), Spring Boot 3.4.0 (LTS), Spring Cloud 2024.0.0, MySQL Driver, Spring Data JPA, Flyway 10.x, Lombok, Spring Web.

---

## 1. PROTOCOLO DE LECTURA OBLIGATORIA (READ-FIRST)
Antes de proponer código, diseñar una BD o modificar cualquier módulo, el agente DEBE:
1. Leer `.agent/architecture-rules.md` → validar que ninguna decisión viola las reglas de arquitectura.
2. Leer `.agent/global-business-rules.md` → validar coherencia con el modelo de negocio y multitenancy.
3. Leer `.agent/modules/{modulo-afectado}.md` → entender el estado actual del módulo.
4. Solo si todos los archivos validan la propuesta → proceder a generar código o diseño.

---

## 2. PROTOCOLO DE ESCRITURA OBLIGATORIA (WRITE-AFTER)
Al finalizar el diseño o implementación de cualquier módulo, el agente DEBE:
1. Crear o actualizar `.agent/modules/{nombre-del-modulo}.md` con la estructura canónica.
2. Si se introducen nuevas reglas de negocio → actualizar `.agent/global-business-rules.md`.
3. Si se establece un nuevo patrón arquitectónico → actualizar `.agent/architecture-rules.md`.
4. Si el módulo expone eventos de dominio que otros módulos consumen → documentar en la sección **Eventos de Dominio**.

---

## 3. PROTOCOLO DE CREACIÓN DE DOMINIO (MCP-01)
Flujo estricto para diseñar un nuevo módulo partiendo de la base existente:

1. **Estructura:** Partir de la estructura de paquetes actual (`domain/model`, `domain/port`, etc.).
2. **Value Objects:** Diseñarlos antes que las Entidades usando `record` de Java 25 para garantizar inmutabilidad.
3. **Aggregate Root:** Identificar la Raíz del Agregado.
4. **AISLAMIENTO TOTAL (Cero Frameworks):** El dominio NO debe contener `@Entity`, `@Table`, `@Autowired`, ni dependencias de Jackson o Spring.
5. **Aprobación:** Exponer el modelo para revisión. NO escribir código de aplicación ni infraestructura hasta que el dominio sea aprobado.

### ESTADO ACTUAL

- **[COMPLETADO]** Core_Empresa, Inventory, Api_Tienda, Billing, POS construidos y probados.
- **[COMPLETADO]** Integración de Bounded Contexts y limpieza del monolith `Api_Tienda`.
- **[COMPLETADO]** Orquestación Global mediante API Gateway (Puerto 8000) configurada.
- **[COMPLETADO]** Estabilización de infraestructura con downgrade estratégico a Stack LTS (Java 21, SB 3.4.0) bajo **ADR-011**.
- **[COMPLETADO]** Validación estricta de esquemas JPA (`ddl-auto=validate`) compatible con Flyway (migraciones en `Api_Tienda`).
- **[COMPLETADO]** Smoke Test E2E containerizado exitoso. Gateway ruteando y protegiendo accesos con JWT.
- **[COMPLETADO]** Suite de Pruebas E2E Live:
  - `tests/e2e/cliente_cero_flow.sh`: Valida la orquestación SCM del "Big Bang" multitenant (Tenant -> Bodega -> Caja) y facturación (DIAN).
  - `tests/e2e/inspector_cuarentena_flow.sh`: Valida el ciclo de Logística Inversa (Devolución -> Cuarentena -> Inspección -> Transferencia doble).

### SIGUIENTES PASOS SUGERIDOS

### Reglas de Naming para Bounded Contexts
| Elemento         | Convención                              | Ejemplo                        |
|------------------|-----------------------------------------|--------------------------------|
| Agregado         | `PascalCase` sustantivo singular        | `Empresa`, `Pedido`, `Caja`    |
| Value Object     | `PascalCase` descriptivo                | `Ruc`, `Email`, `Dinero`       |
| Domain Event     | `PascalCase` verbo en pasado + Dominio  | `EmpresaCreada`, `PedidoCerrado` |
| Port (Input)     | `{Accion}{Entidad}UseCase`              | `CrearEmpresaUseCase`          |
| Port (Output)    | `{Entidad}Repository`                   | `EmpresaRepository`            |
| Service (App)    | `{Accion}{Entidad}Service`              | `CrearEmpresaService`          |
| Adapter (Infra)  | `{Entidad}{Tecnología}Adapter`          | `EmpresaJpaAdapter`            |

---

## 4. PROTOCOLO IAM (KEYCLOAK — CERO CONTRASEÑAS EN BD)
### Principios Absolutos
- **NUNCA** almacenar contraseñas en PostgreSQL/MySQL. Toda autenticación es delegada a Keycloak.
- **NUNCA** emitir tokens JWT propios desde el backend de Spring Boot.
- Spring Boot actúa como **Resource Server** validando tokens Bearer de Keycloak.
- Toda mutación de estado debe adjuntar el `empresa_id` obtenido del token JWT, nunca del payload del cliente.

---

## 5. PROTOCOLO DE COMUNICACIÓN INTER-MÓDULOS
### Regla de Oro: Acoplamiento Mínimo
Los Bounded Contexts NO se comunican directamente a través de llamadas a repositorios ajenos. Jamás comparten tablas en la base de datos.
- Sincrónico: Usa Interfaces (Ports) / API internas.
- Asincrónico: Usa Domain Events (Spring Events / Kafka).

---

## 6. PROTOCOLO DE CERO ALUCINACIONES
Si el usuario solicita algo que viola `architecture-rules.md` o el stack actual (ej. mezclar JPA en el Dominio), el agente DEBE:
1. **DETENER** la generación de código.
2. **ADVERTIR** con el prefijo `⚠️ VIOLACIÓN ARQUITECTÓNICA:`.
3. **FUNDAMENTAR** la solución correcta basada en DDD.
4. **PEDIR CONFIRMACIÓN** antes de continuar.

---

## 7. REGISTRO DE DECISIONES ARQUITECTÓNICAS (ADR)
| ADR-ID  | Fecha       | Decisión                                                                   | Estado      |
|---------|-------------|----------------------------------------------------------------------------|-------------|
| ADR-001 | 2026-08-05  | Adoptar Clean Architecture + Hexagonal + DDD                               | ACEPTADO    |
| ADR-002 | 2026-08-05  | Keycloak como único proveedor de identidad (cero pwd en BD)                | ACEPTADO    |
| ADR-003 | 2026-08-05  | PostgreSQL/MySQL con seguridad por tenant (`empresa_id`)                   | ACEPTADO    |
| ADR-006 | 2026-08-06  | Puerto 8084 asignado exclusivamente a `Api_Tienda`                         | ACEPTADO    |
| ADR-007 | 2026-08-06  | Credenciales DB via variables de entorno — cero hardcoding en properties   | ACEPTADO    |
| ADR-008 | 2026-08-06  | `ddl-auto=validate` estricto con gestión de esquemas Flyway (deuda cerrada) | 🟢 ACEPTADO Y CERRADO |
| ADR-010 | 2026-08-06  | Formato `.properties` preferido sobre `.yaml` (directriz de usuario)      | ACEPTADO    |
| ADR-011 | 2026-08-10  | Downgrade a Stack LTS: Java 21, Spring Boot 3.4.0, Spring Cloud 2024.0.0 para estabilización E2E. Incompatibilidad confirmada con SB 4.0.7 + SC 2025.0.0. | ACEPTADO    |

---

## 8. DIRECTRIZ DE FORMATO DE CONFIGURACIÓN
> **Establecida:** 2026-08-06 | **Alcance:** Todos los módulos del ERP SITFAI
- **Prohibido** generar `application.yml` / `application.yaml` salvo solicitud explícita del usuario.

---

## 9. RESULTADOS PRUEBAS E2E (DOCKER COMPOSE)
> **Fecha:** 2026-08-10 | **Componentes:** Gateway & Backend (LTS)
- **Inicialización de Infraestructura (v3):** Exitosa tras limpieza de volúmenes. MySQL (8.4) e IAM (Keycloak) levantan en estado *Healthy*.
- **Backend (Puerto 8084):** **EXITOSO**. El downgrade a Spring Boot 3.4.0 restauró el ciclo de vida correcto de Flyway. Flyway migró exitosamente la V1 a la V13. La validación estricta de JPA (`ddl-auto=validate`) aprobó el esquema tras mapear correctamente los UUIDs (`spring.jpa.properties.hibernate.type.preferred_uuid_jdbc_type=VARCHAR`). El servicio arrancó correctamente con todos los beans cargados.
- **Gateway (Puerto 8000):** **EXITOSO**. Tras el downgrade a Spring Cloud 2024.0.0, Spring WebFlux inicializa correctamente Netty.
- **Prueba de Humo (Routing):** El Gateway está ruteando correctamente al backend (`401 Unauthorized` indica que la petición llegó al filtro de seguridad JWT del backend a través de la ruta `/api/v1/empresas/**`).
- **Estado Global:** Sistema estabilizado e infraestructura base 100% operativa.