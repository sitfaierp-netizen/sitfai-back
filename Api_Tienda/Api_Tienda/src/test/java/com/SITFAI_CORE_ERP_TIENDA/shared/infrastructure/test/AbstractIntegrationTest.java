package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.test;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.TestcontainersConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.ApplicationEventPublisher;
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
@org.springframework.test.context.event.RecordApplicationEvents
public abstract class AbstractIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    protected org.springframework.test.context.event.ApplicationEvents applicationEvents;

    @org.springframework.beans.factory.annotation.Autowired
    protected ApplicationEventPublisher applicationEventPublisher;

    @MockitoBean
    protected com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.TenantProviderPort purchasingTenantProviderPort;

    @MockitoBean
    protected com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.output.CurrentActorProvider purchasingCurrentActorProvider;

    @MockitoBean
    protected com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.TenantProviderPort posTenantProviderPort;

    @MockitoBean
    protected com.SITFAI_CORE_ERP_TIENDA.pos.application.port.output.CurrentActorProvider posCurrentActorProvider;

    @MockitoBean
    protected com.SITFAI_CORE_ERP_TIENDA.fulfillment.application.port.output.TenantProviderPort fulfillmentTenantProviderPort;

    @MockitoBean
    protected com.SITFAI_CORE_ERP_TIENDA.returns.application.port.output.TenantProviderPort returnsTenantProviderPort;

    @MockitoBean
    protected com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.production.application.port.output.TenantProviderPort productionTenantProviderPort;

    @MockitoBean
    protected com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort inventoryTenantProviderPort;

    @MockitoBean
    protected com.SITFAI_CORE_ERP_TIENDA.core.idempotency.application.port.output.CurrentTenantPort idempotencyCurrentTenantPort;

    @MockitoBean
    protected com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.TenantProviderPort apiTiendaTenantProviderPort;

    @MockitoBean
    protected com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.application.port.output.CurrentActorProvider apiTiendaCurrentActorProvider;



    @MockitoBean
    protected JwtDecoder jwtDecoder;

    @MockitoBean
    protected com.SITFAI_CORE_ERP_TIENDA.core.audit.domain.port.ActorProviderPort auditActorProviderPort;

    @org.junit.jupiter.api.BeforeEach
    void configureDefaultAuditor() {
        org.mockito.Mockito.when(auditActorProviderPort.getCurrentActorId()).thenReturn("integration-test");
    }
}
