package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda;

import org.springframework.context.annotation.Import;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Import(com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration.class)
@org.springframework.test.context.ActiveProfiles("test")
@SpringBootTest
@DisplayName("Integración: Carga de Contexto Spring Boot")
class ApiTiendaApplicationIT {

	@Test
	void contextLoads() {
	}

}
