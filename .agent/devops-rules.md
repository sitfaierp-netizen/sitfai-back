# SITFAI ERP — DevOps y CI/CD
> **Versión:** 1.0.0 | **Fecha:** 2026-09-28 | **Estado:** 🚀 PIPELINE CI IMPLEMENTADO

---

## 1. ESTRATEGIA DE CONTINUOUS INTEGRATION (CI)

El pipeline de CI garantiza la calidad del código, la inmutabilidad de la arquitectura y la seguridad del código fuente en la rama `develop` y `main`.

**Herramienta:** GitHub Actions
**Trigger:** `push` y `pull_request` en `develop` y `main`
**Entorno:** `ubuntu-latest` con Java 21 (Temurin)

---

## 2. ETAPAS DEL PIPELINE (VERIFY)

El workflow utiliza `actions/checkout@v4` para clonar el repositorio y `actions/setup-java@v4` para configurar el JDK con caché de Maven.
El comando base del pipeline es `./mvnw clean verify`, ejecutado explícitamente desde el subdirectorio del módulo (ej. `Api_Tienda/Api_Tienda` configurado vía `working-directory`). Previo a la ejecución, se otorgan permisos de ejecución al wrapper (`chmod +x mvnw`) para asegurar compatibilidad en entornos UNIX.

### 2.1 Pruebas Unitarias y de Integración
- Se ejecutan mediante `maven-surefire-plugin` (unitarias) y `maven-failsafe-plugin` (integración).
- Las pruebas de integración (`*IntegrationTest.java`) utilizan **Testcontainers** para levantar de forma efímera y segura MySQL y Keycloak mediante el demonio de Docker incorporado en `ubuntu-latest`.

### 2.2 Control de Calidad (QA) — JaCoCo
- Se utiliza `jacoco-maven-plugin` durante la fase `test`/`verify`.
- **REGLA ESTRICTA:** La cobertura de código en las clases de Dominio (`com.SITFAI_CORE_ERP_TIENDA.*.domain.*`) debe ser **mayor o igual al 85%**. Si no se cumple, el pipeline falla automáticamente.

### 2.3 Seguridad (OWASP Top 10)
- Se utiliza `dependency-check-maven` para escanear y auditar vulnerabilidades conocidas (CVE) en las dependencias transitivas y directas.
- El escaneo se ejecuta en la fase `verify` como mecanismo de prevención temprana contra ataques en la cadena de suministro de software.

---

## 3. ARTEFACTOS GENERADOS

Al finalizar la ejecución del Workflow (sea exitosa o fallida), se publicarán los siguientes reportes en los **Artifacts** de la corrida en GitHub para su inspección manual:
1. **Reporte JaCoCo:** `jacoco-report` (HTML visualizable de cobertura por líneas e instrucciones).
2. **Reporte Dependency-Check:** `dependency-check-report` (HTML visualizable de hallazgos CVE).

---

> **Nota para el equipo de desarrollo:** Todavía no se ha configurado la fase de despliegue continuo (Continuous Deployment - CD). Cualquier modificación al flujo de release hacia producción será documentada en iteraciones futuras.
