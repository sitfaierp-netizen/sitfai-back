package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.RegistrarIngresoStockUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.DescontarStockUseCase;
import org.springframework.security.oauth2.jwt.JwtDecoder;

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

    @SpyBean
    protected ApplicationEventPublisher applicationEventPublisher;


    @MockBean
    protected com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.TenantProviderPort purchasingTenantProviderPort;

    @MockBean
    protected com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.CurrentActorProvider purchasingCurrentActorProvider;

    @MockBean
    protected com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TenantProviderPort posTenantProviderPort;

    @MockBean
    protected com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.CurrentActorProvider posCurrentActorProvider;

    @MockBean
    protected com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.output.TenantProviderPort fulfillmentTenantProviderPort;

    @MockBean
    protected com.SITFAI_CORE_ERP_TIENDA.returns.application.port.output.TenantProviderPort returnsTenantProviderPort;

    @MockBean
    protected com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.port.output.TenantProviderPort productionTenantProviderPort;

    @MockBean
    protected com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort inventoryTenantProviderPort;

    @MockBean
    protected com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.TenantProviderPort apiTiendaTenantProviderPort;



    @MockBean
    protected RegistrarIngresoStockUseCase registrarIngresoStockUseCase;

    @MockBean
    protected DescontarStockUseCase descontarStockUseCase;

    @MockBean
    protected JwtDecoder jwtDecoder;

    @MockBean
    protected com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort auditActorProviderPort;
}
