package com.SITFAI_CORE_ERP.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SITFAI ERP — API Gateway Perimetral.
 * <p>
 * Punto único de entrada (Reverse Proxy) para clientes web, móviles y externos.
 * Gestiona el enrutamiento reactivo no bloqueante (Spring Cloud Gateway),
 * validación perimetral de JWT emitidos por Keycloak y políticas CORS unificadas.
 * <p>
 * Puerto: 8000
 */
@SpringBootApplication
public class SitfaiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SitfaiGatewayApplication.class, args);
    }
}
