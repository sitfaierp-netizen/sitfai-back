# SITFAI ERP — API Gateway Perimetral (com.SITFAI_CORE_ERP.gateway)
> **Versión:** 1.0.0 | **Fecha:** 2026-08-10 | **Estado:** 🟢 OPERATIVO

---

## 1. IDENTIDAD DEL MÓDULO

| Campo              | Valor                                         |
|--------------------|-----------------------------------------------|
| **Group ID**       | `com.SITFAI_CORE_ERP`                         |
| **Artifact ID**    | `gateway`                                     |
| **Package raíz**   | `com.SITFAI_CORE_ERP.gateway`                 |
| **Puerto Oficial** | `8000`                                        |
| **Framework Base** | Spring Cloud Gateway (Spring Boot 4 / WebFlux)|
| **Java**           | `25`                                          |
| **Seguridad**      | Reactive OAuth2 Resource Server (Keycloak JWT)|
| **CORS**           | Centralizado globalmente                      |

---

## 2. OBJETIVO DEL COMPONENTE

El `gateway` es el **Punto Único de Entrada (Reverse Proxy & Edge Gateway)** para todos los clientes (SPAs web, aplicaciones móviles, sistemas externos, integraciones de terceros).

### Responsabilidades Clave:
1. **Enrutamiento Dinámico Reactivo:** Despacho no bloqueante de peticiones HTTP hacia los Bounded Contexts internos según el prefijo de URI.
2. **Seguridad Perimetral:** Validación previa de tokens JWT emitidos por el IAM Keycloak (`http://localhost:8080/realms/sitfai-erp`).
3. **CORS Global:** Centralización de encabezados de intercambio cruzado para evitar configuraciones redundantes en cada microservicio.
4. **Ocultamiento Topológico:** Los microservicios internos (`8084`, `8085`, `8086`) quedan protegidos de la exposición pública directa.

---

## 3. MAPA OFICIAL DE ENRUTAMIENTO (PORT MAP)

```text
                                  ┌────────────────────────┐
                                  │      Keycloak IAM      │
                                  │      Puerto: 8080      │
                                  └───────────┬────────────┘
                                              │ (JWT Emisión)
                                              ▼
                             ┌──────────────────────────────────┐
                             │       SITFAI API GATEWAY         │
                             │          Puerto: 8000            │
                             └────────────────┬─────────────────┘
                                              │
         ┌──────────────────────────────┼──────────────────────────────┬──────────────────────────────┼──────────────────────────────┐
         │ /api/v1/empresas/**          │ /api/v1/inventory/**         │ /api/v1/pedidos/**           │ /api/v1/billing/**           │ /api/v1/pos/**
         │                              │                              │ /api/v1/productos/**         │                              │
         ▼                              ▼                              ▼                              ▼                              ▼
┌──────────────────┐           ┌──────────────────┐           ┌──────────────────┐           ┌──────────────────┐           ┌──────────────────┐
│   core-empresa   │           │    inventory     │           │    Api_Tienda    │           │     billing      │           │       pos        │
│   Puerto: 8081   │           │   Puerto: 8083   │           │   Puerto: 8084   │           │   Puerto: 8085   │           │   Puerto: 8086   │
└──────────────────┘           └──────────────────┘           └──────────────────┘           └──────────────────┘           └──────────────────┘
```

| ID de Ruta | Patrón de URI | Servicio Destino | Variable de Entorno / Fallback |
|---|---|---|---|
| `core-empresa-route` | `/api/v1/empresas/**` | `core-empresa` | `${CORE_EMPRESA_SERVICE_URL:http://localhost:8081}` |
| `inventory-route` | `/api/v1/inventory/**` | `inventory` | `${INVENTORY_SERVICE_URL:http://localhost:8083}` |
| `tienda-pedidos-route` | `/api/v1/pedidos/**` | `Api_Tienda` | `${TIENDA_SERVICE_URL:http://localhost:8084}` |
| `tienda-productos-route` | `/api/v1/productos/**` | `Api_Tienda` | `${TIENDA_SERVICE_URL:http://localhost:8084}` |
| `billing-facturas-route` | `/api/v1/billing/**` | `billing` | `${BILLING_SERVICE_URL:http://localhost:8085}` |
| `pos-turnos-route` | `/api/v1/pos/**` | `pos` | `${POS_SERVICE_URL:http://localhost:8086}` |

---

## 4. ESTRUCTURA DEL MÓDULO

```text
gateway/
├── pom.xml
├── mvnw / mvnw.cmd
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/SITFAI_CORE_ERP/gateway/
    │   │       ├── SitfaiGatewayApplication.java (Clase principal @SpringBootApplication)
    │   │       └── config/
    │   │           └── SecurityConfig.java       (Filtro reactivo WebFlux & OAuth2 JWT)
    │   └── resources/
    │       └── application.properties            (Enrutamiento dinámico, puertos y CORS)
    └── test/
        └── java/
            └── com/SITFAI_CORE_ERP/gateway/
                └── SitfaiGatewayApplicationTests.java
```

---

## 5. CONFIGURACIÓN DE PROPIEDADES (`application.properties`)

```properties
server.port=8000
spring.application.name=sitfai-gateway

# Keycloak JWT Resource Server
spring.security.oauth2.resourceserver.jwt.issuer-uri=${KEYCLOAK_ISSUER_URI:http://localhost:8080/realms/sitfai-erp}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${KEYCLOAK_JWK_URI:http://localhost:8080/realms/sitfai-erp/protocol/openid-connect/certs}

# Enrutamiento Reactivo
spring.cloud.gateway.routes[0].id=core-empresa-route
spring.cloud.gateway.routes[0].uri=${CORE_EMPRESA_SERVICE_URL:http://localhost:8081}
spring.cloud.gateway.routes[0].predicates[0]=Path=/api/v1/empresas/**

spring.cloud.gateway.routes[1].id=inventory-route
spring.cloud.gateway.routes[1].uri=${INVENTORY_SERVICE_URL:http://localhost:8083}
spring.cloud.gateway.routes[1].predicates[0]=Path=/api/v1/inventory/**

spring.cloud.gateway.routes[2].id=tienda-pedidos-route
spring.cloud.gateway.routes[2].uri=${TIENDA_SERVICE_URL:http://localhost:8084}
spring.cloud.gateway.routes[2].predicates[0]=Path=/api/v1/pedidos/**

spring.cloud.gateway.routes[3].id=tienda-productos-route
spring.cloud.gateway.routes[3].uri=${TIENDA_SERVICE_URL:http://localhost:8084}
spring.cloud.gateway.routes[3].predicates[0]=Path=/api/v1/productos/**

spring.cloud.gateway.routes[4].id=billing-facturas-route
spring.cloud.gateway.routes[4].uri=${BILLING_SERVICE_URL:http://localhost:8085}
spring.cloud.gateway.routes[4].predicates[0]=Path=/api/v1/billing/**

spring.cloud.gateway.routes[5].id=pos-turnos-route
spring.cloud.gateway.routes[5].uri=${POS_SERVICE_URL:http://localhost:8086}
spring.cloud.gateway.routes[5].predicates[0]=Path=/api/v1/pos/**

# CORS Global
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:4200}
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-methods=GET,POST,PUT,DELETE,PATCH,OPTIONS
spring.cloud.gateway.globalcors.cors-configurations.[/**].allowed-headers=*
spring.cloud.gateway.globalcors.cors-configurations.[/**].allow-credentials=false
spring.cloud.gateway.globalcors.cors-configurations.[/**].max-age=3600
```
