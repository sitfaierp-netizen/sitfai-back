package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.mockito.Mockito;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    static final MySQLContainer<?> MYSQL_CONTAINER;

    static {
        MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.4.0")
                .withDatabaseName("sitfai_erp")
                .withUsername("sitfai")
                .withPassword("sitfaipass");
        MYSQL_CONTAINER.start();
    }

    @Bean
    @ServiceConnection
    public MySQLContainer<?> mysqlContainer() {
        return MYSQL_CONTAINER;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return Mockito.mock(JwtDecoder.class);
    }
}
