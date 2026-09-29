package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Clase base unificada para todas las pruebas de integración.
 * Su objetivo es consolidar el contexto de Spring Boot para evitar el 
 * "Context Pollution" (creación múltiple de contextos y contenedores Docker),
 * resolviendo así los problemas de consumo de memoria y concurrencia.
 */
@SpringBootTest(classes = ApiTiendaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
public abstract class AbstractIntegrationTest {

}
