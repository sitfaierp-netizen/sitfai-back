# SITFAI ERP — Módulos Registrados
> **Directorio de módulos diseñados e implementados**
> **Última actualización:** 2026-09-28 | **Estado global:** 🟢 BACKEND CONGELADO Y ESTABILIZADO

| Módulo                  | Archivo                              | Estado              | Versión | Puerto |
|-------------------------|--------------------------------------|---------------------|---------|--------|
| `core-empresa`          | `core-empresa.md`                    | 🟢 IMPLEMENTADO     | 1.1.0   | 8081   |
| `iam-module`            | `iam-module.md`                      | 🟢 IMPLEMENTADO     | 1.0.0   | 8082   |
| `inventory` (WMS)       | `inventory.md`                       | 🟢 IMPLEMENTADO     | 1.0.0   | 8083   |
| `api-tienda` (Pedidos)  | `api-tienda.md`                      | 🟢 IMPLEMENTADO     | 1.0.0   | 8084   |
| `billing` (DIAN)        | `billing.md`                         | 🟢 IMPLEMENTADO     | 1.0.0   | 8085   |
| `pos`                   | `pos.md`                             | 🟢 IMPLEMENTADO     | 2.1.0   | 8086   |
| `purchasing`            | `purchasing.md`                      | 🟢 IMPLEMENTADO     | 1.0.0   | —      |
| `fulfillment`           | `fulfillment.md`                     | 🟢 IMPLEMENTADO     | 1.0.0   | —      |
| `core-audit`            | `core-audit.md`                      | 🟢 IMPLEMENTADO     | 1.0.0   | —      |
| `gateway`               | `gateway.md`                         | 🟢 IMPLEMENTADO     | 1.0.0   | 8000   |

---

> **Política de desarrollo:** Backend CONGELADO. Stack LTS: Java 21, Spring Boot 3.4.0, Spring Cloud 2024.0.0 (ADR-011).
> Siguiente fase: Integración Frontend (sitfai-erp-front → Angular + Keycloak-js) apuntando al Gateway :8000.
