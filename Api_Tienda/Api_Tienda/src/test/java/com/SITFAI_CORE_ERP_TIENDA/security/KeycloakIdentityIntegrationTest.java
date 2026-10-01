package com.SITFAI_CORE_ERP_TIENDA.security;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Path;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(classes = ApiTiendaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = true)
class KeycloakIdentityIntegrationTest {

    private static final String REALM = "sitfai-erp";
    private static final String SERVICE_CLIENT = "sitfai-backend-admin";
    private static final String SERVICE_SECRET = "test-only-service-secret-not-for-production";
    private static final String LOGIN_CLIENT = "api-tienda-client";
    private static final String GLOBAL_ADMIN = "blockb-global-admin";
    private static final String GLOBAL_PASSWORD = "BlockB-Test-Only-Global-Password-42!";
    private static final String TENANT_PASSWORD = "BlockB-Test-Only-Tenant-Password-42!";
    private static final Set<String> MANAGED_ROLES = Set.of(
            "SUPER_ADMIN", "EMPRESA_ADMIN", "SUCURSAL_MANAGER", "BODEGA_OPERATOR", "CAJERO");
    private static final Network KEYCLOAK_NETWORK = Network.newNetwork();

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4.0")
            .withDatabaseName("sitfai_keycloak_e2e")
            .withUsername("sitfai")
            .withPassword("sitfaipass");

    @Container
    static final GenericContainer<?> MAILPIT = new GenericContainer<>("axllent/mailpit:v1.27")
            .withNetwork(KEYCLOAK_NETWORK)
            .withNetworkAliases("smtp")
            .withExposedPorts(8025)
            .waitingFor(Wait.forListeningPort());

    @Container
    static final GenericContainer<?> KEYCLOAK = new GenericContainer<>("quay.io/keycloak/keycloak:26.0.0")
            .withEnv("KC_BOOTSTRAP_ADMIN_USERNAME", "test-only-bootstrap")
            .withEnv("KC_BOOTSTRAP_ADMIN_PASSWORD", "test-only-bootstrap-password")
            .withEnv("SITFAI_BACKEND_ADMIN_CLIENT_SECRET", SERVICE_SECRET)
            .withEnv("JAVA_OPTS_APPEND", "-Dkeycloak.migration.replace-placeholders=true")
            .withEnv("KEYCLOAK_SMTP_HOST", "smtp")
            .withEnv("KEYCLOAK_SMTP_PORT", "1025")
            .withEnv("KEYCLOAK_SMTP_FROM", "identity@sitfai.test")
            .withEnv("KEYCLOAK_SMTP_AUTH", "false")
            .withEnv("KEYCLOAK_SMTP_STARTTLS", "false")
            .withEnv("KEYCLOAK_SMTP_SSL", "false")
            .withEnv("KEYCLOAK_SMTP_USERNAME", "")
            .withEnv("KEYCLOAK_SMTP_PASSWORD", "")
            .withNetwork(KEYCLOAK_NETWORK)
            .withCopyFileToContainer(
                    MountableFile.forHostPath(Path.of("..", "..", "docker", "keycloak", "realm-export.json")
                            .toAbsolutePath().normalize()),
                    "/opt/keycloak/data/import/realm-export.json")
            .withCommand("start-dev", "--import-realm")
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/realms/" + REALM).forStatusCode(200)
                    .withStartupTimeout(Duration.ofMinutes(3)));

    @DynamicPropertySource
    static void keycloakProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", KeycloakIdentityIntegrationTest::realmUrl);
        registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri",
                () -> realmUrl() + "/protocol/openid-connect/certs");
        registry.add("keycloak.admin.server-url", KeycloakIdentityIntegrationTest::serverUrl);
        registry.add("keycloak.admin.auth-realm", () -> REALM);
        registry.add("keycloak.admin.realm", () -> REALM);
        registry.add("keycloak.admin.client-id", () -> SERVICE_CLIENT);
        registry.add("keycloak.admin.client-secret", () -> SERVICE_SECRET);
        registry.add("keycloak.admin.connect-timeout", () -> "2s");
        registry.add("keycloak.admin.read-timeout", () -> "10s");
        registry.add("outbox.poller.initial-delay-ms", () -> "3600000");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private Keycloak serviceAccount;

    @Test
    void hardensProvisioningAuthorizationLifecycleAndRetryAgainstRealKeycloak() throws Exception {
        createLoginUser(GLOBAL_ADMIN, "global-admin@sitfai.test", null, "SUPER_ADMIN", GLOBAL_PASSWORD);
        String globalToken = passwordToken(GLOBAL_ADMIN, GLOBAL_PASSWORD);

        UUID empresaA = createEmpresa(globalToken, "91000000001", "Block B Tenant A");
        UUID empresaB = createEmpresa(globalToken, "91000000002", "Block B Tenant B");
        UUID tenantAdminId = UUID.randomUUID();
        UUID cashierId = UUID.randomUUID();

        registerUser(globalToken, tenantAdminId, empresaA, "blockb-admin-a", "admin-a@sitfai.test", "EMPRESA_ADMIN");
        registerUser(globalToken, cashierId, empresaB, "blockb-cashier-b", "cashier-b@sitfai.test", "CAJERO");
        assertOnboardingMailDelivered("admin-a@sitfai.test");
        assertOnboardingMailDelivered("cashier-b@sitfai.test");

        UserRepresentation provisioned = externalUser("blockb-admin-a");
        assertThat(provisioned.isEnabled()).isTrue();
        assertThat(provisioned.getAttributes()).containsEntry("empresa_id", List.of(empresaA.toString()));
        assertThat(provisioned.getAttributes()).containsEntry("sitfai_usuario_id", List.of(tenantAdminId.toString()));
        assertThat(provisioned.getRequiredActions()).containsExactly("UPDATE_PASSWORD");
        assertThat(serviceAccount.realm(REALM).users().get(provisioned.getId()).credentials()).isEmpty();
        assertThat(managedRoles(provisioned)).containsExactly("EMPRESA_ADMIN");
        assertThatThrownBy(() -> passwordToken("blockb-admin-a", "blockb-admin-a"))
                .isInstanceOf(RuntimeException.class);

        setTestPasswordAndCompleteAction("blockb-admin-a", TENANT_PASSWORD);
        setTestPasswordAndCompleteAction("blockb-cashier-b", TENANT_PASSWORD);
        String tenantAdminToken = passwordToken("blockb-admin-a", TENANT_PASSWORD);
        String cashierToken = passwordToken("blockb-cashier-b", TENANT_PASSWORD);

        mockMvc.perform(get("/iam/usuarios")
                        .queryParam("empresaId", empresaA.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tenantAdminToken)))
                .andExpect(status().isOk());
        mockMvc.perform(get("/iam/usuarios")
                        .queryParam("empresaId", empresaB.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tenantAdminToken)))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/iam/usuarios")
                        .queryParam("empresaId", empresaB.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(cashierToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(patch("/iam/usuarios/{id}/rol", tenantAdminId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(globalToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empresaId\":\"" + empresaA + "\",\"nuevoRol\":\"SUCURSAL_MANAGER\"}"))
                .andExpect(status().isOk());
        assertThat(managedRoles(externalUser("blockb-admin-a"))).containsExactly("SUCURSAL_MANAGER");

        mockMvc.perform(patch("/iam/usuarios/{id}/desactivar", tenantAdminId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(globalToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empresaId\":\"" + empresaA + "\",\"motivo\":\"E2E Block B\"}"))
                .andExpect(status().isOk());
        assertThat(externalUser("blockb-admin-a").isEnabled()).isFalse();
        assertThatThrownBy(() -> passwordToken("blockb-admin-a", TENANT_PASSWORD))
                .isInstanceOf(RuntimeException.class);

        mockMvc.perform(patch("/iam/usuarios/{id}/reactivar", tenantAdminId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(globalToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"empresaId\":\"" + empresaA + "\"}"))
                .andExpect(status().isOk());
        assertThat(externalUser("blockb-admin-a").isEnabled()).isTrue();
        assertThat(passwordToken("blockb-admin-a", TENANT_PASSWORD)).isNotBlank();

        reconcile(globalToken, empresaA, tenantAdminId, status().isOk());
        reconcile(globalToken, empresaA, tenantAdminId, status().isOk());
        assertThat(serviceAccount.realm(REALM).users().searchByUsername("blockb-admin-a", true))
                .hasSize(1);

        KEYCLOAK.getDockerClient().pauseContainerCmd(KEYCLOAK.getContainerId()).exec();
        try {
            reconcile(globalToken, empresaA, tenantAdminId, status().isServiceUnavailable());
        } finally {
            KEYCLOAK.getDockerClient().unpauseContainerCmd(KEYCLOAK.getContainerId()).exec();
        }
        reconcile(globalToken, empresaA, tenantAdminId, status().isOk());
        assertThat(serviceAccount.realm(REALM).users().searchByUsername("blockb-admin-a", true))
                .hasSize(1);
    }

    private UUID createEmpresa(String token, String ruc, String razonSocial) throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ruc\":\"" + ruc + "\",\"razonSocial\":\"" + razonSocial + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(json(result).get("id").asText());
    }

    private void registerUser(
            String token,
            UUID id,
            UUID empresaId,
            String username,
            String email,
            String role
    ) throws Exception {
        mockMvc.perform(post("/iam/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"" + id + "\",\"empresaId\":\"" + empresaId
                                + "\",\"username\":\"" + username + "\",\"email\":\"" + email
                                + "\",\"rol\":\"" + role + "\"}"))
                .andExpect(status().isCreated());
    }

    private void reconcile(
            String token,
            UUID empresaId,
            UUID usuarioId,
            org.springframework.test.web.servlet.ResultMatcher expectedStatus
    ) throws Exception {
        mockMvc.perform(patch("/iam/usuarios/{id}/reconciliar-identidad", usuarioId)
                        .queryParam("empresaId", empresaId.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(expectedStatus);
    }

    private void createLoginUser(String username, String email, UUID empresaId, String role, String password) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEmail(email);
        user.setFirstName("Block B");
        user.setLastName("E2E User");
        user.setEmailVerified(true);
        user.setEnabled(true);
        user.setRequiredActions(List.of());
        if (empresaId != null) {
            user.setAttributes(Map.of("empresa_id", List.of(empresaId.toString())));
        }
        String id;
        try (var response = serviceAccount.realm(REALM).users().create(user)) {
            id = CreatedResponseUtil.getCreatedId(response);
        }
        var resource = serviceAccount.realm(REALM).users().get(id);
        resource.resetPassword(password(password));
        RoleRepresentation realmRole = serviceAccount.realm(REALM).roles().get(role).toRepresentation();
        resource.roles().realmLevel().add(List.of(realmRole));
    }

    private void setTestPasswordAndCompleteAction(String username, String password) {
        UserRepresentation user = externalUser(username);
        var resource = serviceAccount.realm(REALM).users().get(user.getId());
        resource.resetPassword(password(password));
        user.setRequiredActions(List.of());
        resource.update(user);
    }

    private CredentialRepresentation password(String value) {
        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(value);
        credential.setTemporary(false);
        return credential;
    }

    private UserRepresentation externalUser(String username) {
        return serviceAccount.realm(REALM).users().searchByUsername(username, true).stream()
                .filter(user -> username.equals(user.getUsername()))
                .findFirst()
                .orElseThrow();
    }

    private List<String> managedRoles(UserRepresentation user) {
        return serviceAccount.realm(REALM).users().get(user.getId()).roles().realmLevel().listAll().stream()
                .map(RoleRepresentation::getName)
                .filter(MANAGED_ROLES::contains)
                .sorted()
                .toList();
    }

    private String passwordToken(String username, String password) {
        try (Keycloak login = KeycloakBuilder.builder()
                .serverUrl(serverUrl())
                .realm(REALM)
                .clientId(LOGIN_CLIENT)
                .username(username)
                .password(password)
                .grantType(OAuth2Constants.PASSWORD)
                .build()) {
            return login.tokenManager().getAccessTokenString();
        }
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
    }

    private void assertOnboardingMailDelivered(String recipient) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        URI messagesUri = URI.create("http://" + MAILPIT.getHost() + ":" + MAILPIT.getMappedPort(8025)
                + "/api/v1/search?query=to:" + recipient);
        for (int attempt = 0; attempt < 20; attempt++) {
            HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder(messagesUri).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200
                    && objectMapper.readTree(response.body()).path("total").asInt() > 0) {
                return;
            }
            Thread.sleep(250);
        }
        throw new AssertionError("No se recibió el correo de onboarding para " + recipient);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private static String serverUrl() {
        return "http://" + KEYCLOAK.getHost() + ":" + KEYCLOAK.getMappedPort(8080);
    }

    private static String realmUrl() {
        return serverUrl() + "/realms/" + REALM;
    }
}
