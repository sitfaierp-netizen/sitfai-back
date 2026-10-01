package com.SITFAI_CORE_ERP_TIENDA.security;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionConfigurationSecurityTest {

    @Test
    void databasePasswordMustBeRequiredWithoutPredictableFallback() throws IOException {
        try (var input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            assertThat(input).as("application.properties debe existir").isNotNull();
            String properties = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(properties)
                    .contains("spring.datasource.password=${DB_PASSWORD}")
                    .doesNotContain("${DB_PASSWORD:")
                    .doesNotContain("sitfai_secret_pwd");
        }
    }
}
