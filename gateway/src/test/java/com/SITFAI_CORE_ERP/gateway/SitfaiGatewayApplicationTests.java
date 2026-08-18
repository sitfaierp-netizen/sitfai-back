package com.SITFAI_CORE_ERP.gateway;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://auth.sitfai.test/realms/sitfai-erp",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=https://auth.sitfai.test/realms/sitfai-erp/protocol/openid-connect/certs"
})
@DisplayName("API Gateway: Inicialización del Contexto Spring Cloud Gateway")
class SitfaiGatewayApplicationTests {

    @Test
    @DisplayName("El contexto de Spring Cloud Gateway y WebFlux debe cargar correctamente")
    void contextLoads() {
        // Verifica que la configuración de rutas y seguridad reactiva inicialicen sin errores
    }
}
