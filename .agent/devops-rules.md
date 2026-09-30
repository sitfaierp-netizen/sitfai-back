# SITFAI ERP — DevOps y CI/CD
> **Versión:** 1.1.0 | **Fecha:** 2026-09-29 | **Estado:** CI estabilizado con baseline de cobertura

---

## 1. Estrategia de Continuous Integration (CI)

El pipeline valida calidad, arquitectura y seguridad en `develop` y `master`, tanto para `push` como para `pull_request`.

**Herramienta:** GitHub Actions
**Entorno:** `ubuntu-latest` con Java 21 (Temurin)
**Acciones:** `actions/checkout@v7`, `actions/setup-java@v6` con caché Maven y `actions/upload-artifact@v7`.

El comando base es `./mvnw clean verify` desde `Api_Tienda/Api_Tienda`; el workflow habilita previamente el wrapper para entornos UNIX.

## 2. Pruebas y cobertura

- Surefire ejecuta unitarias (`*Test.java`) y excluye `*IT.java` y `*IntegrationTest*.java`.
- Failsafe ejecuta integración una sola vez (`*IntegrationTest.java`), incluyendo los contextos que usan Testcontainers/MySQL cuando corresponde.
- Ambos conservan el agente JaCoCo mediante `@{argLine}` y escriben en el mismo `jacoco.exec` con `append=true`.
- El informe y la validación JaCoCo se generan en `verify`, después de Failsafe: la métrica es combinada, no únicamente unitaria.

### Política del Domain Layer: baseline + ratchet + objetivo

El alcance exclusivo son las clases compiladas bajo `com/SITFAI_CORE_ERP_TIENDA/**/domain/**/*.class`; no se agregan clases de aplicación o infraestructura para mejorar artificialmente la métrica, ni se excluyen clases de dominio.

| Nivel | Valor | Aplicación |
|---|---:|---|
| Baseline bloqueante actual | 64% de instrucciones cubiertas | JaCoCo `check` sobre el bundle de dominio completo; la suite consolidada midió 64,9873% al establecerla. |
| Ratchet | Nunca disminuir el baseline | Cada aumento real de cobertura debe elevar `jacoco.domain.minimum`. |
| Objetivo de calidad | 85% | Documentado como meta; no bloquea aún hasta alcanzarse de forma sostenible. |

La revisión de cobertura debe conservar esta separación. No se aceptan tests masivos, triviales, por reflexión ni exclusiones artificiales para alcanzar el objetivo.

## 3. Seguridad y artefactos

`dependency-check-maven` ejecuta su análisis OWASP en `verify`. Al finalizar, incluso si falla el job, se publican:

1. `jacoco-report`: informe HTML consolidado de JaCoCo.
2. `dependency-check-report`: informe HTML de vulnerabilidades conocidas.

El gate conserva NVD y CISA como fuentes obligatorias. OSS Index es enriquecimiento remoto y permanece deshabilitado mientras el repositorio no disponga de credenciales válidas: su endpoint rechazó las consultas anónimas con HTTP 401 y no debe bloquear el informe basado en NVD. Al provisionar un `serverId` con credenciales válidas puede reactivarse. `NVD_API_KEY` es un secreto opcional de GitHub Actions; si se configura, Dependency-Check lo utiliza sin exponerlo en el log. La caché de su base de datos se conserva entre ejecuciones para que una actualización completa de NVD no se repita innecesariamente.

No hay CD configurado. Cualquier cambio al despliegue productivo se documentará en una iteración posterior.
