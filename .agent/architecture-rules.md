# SITFAI ERP — Reglas Inquebrantables de Arquitectura
> **Versión:** 1.0.0 | **Fecha:** 2026-08-05 | **Revisión:** Obligatoria antes de cualquier cambio estructural

---

## PRINCIPIO RECTOR

Este sistema adopta **Clean Architecture**, **Arquitectura Hexagonal (Ports & Adapters)** y **Domain-Driven Design (DDD)**.
La regla cardinal es: **las dependencias de código siempre apuntan hacia adentro (hacia el dominio)**.
El dominio NO conoce, NO importa y NO depende de ningún framework, base de datos, o sistema externo.

---

## REGLA 1 — CAPAS Y SUS RESPONSABILIDADES (INQUEBRANTABLE)

### Layer Stack por Módulo

```
┌──────────────────────────────────────────────────────────────┐
│                    INFRASTRUCTURE LAYER                       │
│  (Adaptadores: JPA, REST Controllers, Kafka, Email, etc.)    │
├──────────────────────────────────────────────────────────────┤
│                    APPLICATION LAYER                          │
│  (Use Cases / Application Services / Command & Query)        │
├──────────────────────────────────────────────────────────────┤
│                      DOMAIN LAYER                             │
│  (Aggregates, Entities, Value Objects, Domain Events,        │
│   Domain Services, Ports — interfaces puras, sin imports)    │
└──────────────────────────────────────────────────────────────┘
```

### Domain Layer — LO QUE NUNCA PUEDE CONTENER

| ❌ PROHIBIDO                          | ✅ ALTERNATIVA CORRECTA                    |
|--------------------------------------|--------------------------------------------|
| `import jakarta.persistence.*`       | POJOs puros — el mapeo va en Infra         |
| `import org.springframework.*`       | Interfaces de puertos (sin Spring)         |
| `import com.fasterxml.jackson.*`     | Serialización en el adaptador de Infra     |
| `import lombok.*` (anotaciones JPA)  | Solo `@Value` / constructores explícitos   |
| Lógica de consulta SQL/JPQL          | Método en el puerto `Repository`           |
| Llamadas a APIs externas             | Puerto de salida + adaptador en Infra      |
| Acceso directo a `SecurityContext`   | El tenant se inyecta como parámetro        |

### Application Layer — Responsabilidades

- **ÚNICO lugar** donde se orquestan los casos de uso.
- Instancia agregados a través de factory methods del dominio.
- Llama a puertos de salida (repositorios, event publishers) — solo interfaces.
- **NO** contiene lógica de negocio (esa va en el Dominio).
- **NO** contiene lógica de transformación HTTP (esa va en el adaptador REST).

### Infrastructure Layer — Responsabilidades

- Implementa los puertos definidos en el Dominio/Aplicación.
- Contiene: JPA Entities, Spring Data Repositories, REST Controllers, Kafka Consumers/Producers.
- Los **REST Controllers** son adaptadores de entrada (Driving Adapters).
- Los **JPA Adapters** son adaptadores de salida (Driven Adapters).

---

## REGLA 2 — ESTRUCTURA DE PAQUETES (INQUEBRANTABLE)

```
com.sitfai.erp.{modulo}/
├── domain/
│   ├── model/              # Aggregates, Entities, Value Objects
│   ├── event/              # Domain Events
│   ├── exception/          # Domain Exceptions (sin framework)
│   ├── service/            # Domain Services (lógica sin estado)
│   └── port/
│       ├── input/          # Use Case interfaces (Driving Ports)
│       └── output/         # Repository & Event Publisher interfaces (Driven Ports)
├── application/
│   ├── service/            # Implementaciones de los Use Cases
│   ├── dto/                # Command/Query DTOs (sin anotaciones JPA/Jackson)
│   └── mapper/             # Mappers dominio ↔ DTO (Application)
└── infrastructure/
    ├── adapter/
    │   ├── in/
    │   │   ├── web/        # REST Controllers (@RestController)
    │   │   └── messaging/  # Kafka Consumers
    │   └── out/
    │       ├── persistence/ # JPA Entities, Spring Data Repos, JPA Adapters
    │       └── messaging/   # Kafka Producers
    ├── config/             # Spring @Configuration classes
    └── mapper/             # Mappers dominio ↔ JPA Entity
```

---

## REGLA 3 — MODELADO DDD (INQUEBRANTABLE)

### Agregados

- Cada **Agregado** tiene una **Aggregate Root** con ID de tipo Value Object (no `Long` primitivo).
- El estado interno del Agregado solo se modifica a través de sus métodos (encapsulamiento total).
- Los Agregados emiten **Domain Events** al final de la operación (usando un campo `List<DomainEvent>`).
- **NUNCA** se pasa una referencia completa de otro Agregado; se pasa su **ID**.

```java
// ✅ CORRECTO
public class Pedido {
    private PedidoId id;
    private EmpresaId empresaId;      // Solo el ID del otro agregado
    private List<DomainEvent> events; // Eventos acumulados
}

// ❌ INCORRECTO
public class Pedido {
    private Long id;                  // Tipo primitivo — NO
    private Empresa empresa;          // Referencia cruzada — NO
}
```

### Value Objects

- Son **inmutables** (sin setters, `final` fields).
- Implementan `equals()` y `hashCode()` por valor.
- Contienen su propia validación (fail-fast en el constructor).

```java
// ✅ CORRECTO
public record Ruc(String valor) {
    public Ruc {
        if (valor == null || !valor.matches("\\d{11}")) {
            throw new RucInvalidoException(valor);
        }
    }
}
```

### Domain Events

- Nomenclatura: `{Entidad}{AccionPasado}Event` → `EmpresaCreadaEvent`
- Son **inmutables** (record o clase final).
- Contienen timestamp, ID del agregado que los generó y datos mínimos necesarios.
- **NUNCA** contienen referencias a servicios o repositorios.

---

## REGLA 4 — MULTITENANCY (INQUEBRANTABLE)

- El `empresa_id` (tenant ID) es el **discriminador raíz** de todos los datos.
- Toda consulta a la BD debe incluir el `empresa_id` en el filtro (Row-Level Security en PostgreSQL).
- El tenant context **NUNCA** viene de la BD en el dominio; siempre proviene del JWT de Keycloak.
- Un usuario **NUNCA** puede operar sobre datos de un `empresa_id` distinto al de su token JWT.

---

## REGLA 5 — API REST (INQUEBRANTABLE)

- Los REST Controllers **NUNCA** reciben ni devuelven objetos del dominio directamente.
- Se usan **Request DTOs** (entrada) y **Response DTOs** (salida), distintos de los DTOs de Application.
- Versionado de API obligatorio: `/api/v1/{recurso}`.
- Los errores siguen el estándar **RFC 7807 (Problem Details)**.
- Toda operación de escritura (POST/PUT/PATCH/DELETE) requiere idempotency key para operaciones críticas.

---

## REGLA 6 — BASE DE DATOS (INQUEBRANTABLE)

- Las **JPA Entities** viven EXCLUSIVAMENTE en `infrastructure/adapter/out/persistence/`.
- El dominio NO sabe que existe JPA, Hibernate ni ningún ORM.
- Los IDs en BD son `UUID` (no auto-increment) para soporte multi-nodo.
- Toda migración de esquema se gestiona con **Flyway** (archivos `V{n}__{descripcion}.sql`).
- Naming en BD: `snake_case` para tablas y columnas, prefijadas por esquema de módulo.

---

## REGLA 7 — SEGURIDAD (INQUEBRANTABLE)

- Toda operación protegida requiere un token JWT válido emitido por el Realm `sitfai-erp` de Keycloak.
- La autorización a nivel de método usa `@PreAuthorize` con expresiones de rol de Keycloak.
- Los endpoints de salud y métricas (`/actuator`) están protegidos en producción.
- **CORS** se configura explícitamente; cero wildcards `*` en producción.

---

## REGLA 8 — TESTING (INQUEBRANTABLE)

| Capa         | Tipo de Test           | Herramienta                        |
|--------------|------------------------|------------------------------------|
| Dominio      | Unit Tests             | JUnit 5, sin Spring context        |
| Aplicación   | Unit Tests             | JUnit 5 + Mockito                  |
| Infra (Repo) | Integration Tests      | Testcontainers + PostgreSQL        |
| API REST     | Integration Tests      | MockMvc / WebTestClient            |
| E2E          | Component Tests        | TestContainers (full stack)        |

- Cobertura objetivo del **Domain Layer: 85%**. La puerta de CI parte del baseline medido y se eleva gradualmente sin ocultar clases de dominio; la política vigente se documenta en `.agent/devops-rules.md`.
- Cobertura mínima del **Application Layer: 80%**.
