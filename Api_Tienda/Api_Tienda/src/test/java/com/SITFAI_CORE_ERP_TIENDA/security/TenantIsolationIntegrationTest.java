package com.SITFAI_CORE_ERP_TIENDA.security;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.ApiTiendaApplication;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output.BodegaRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@SpringBootTest(classes = ApiTiendaApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = true)
@Import(TenantIsolationIntegrationTest.SignedJwtTestConfiguration.class)
class TenantIsolationIntegrationTest {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4.0")
            .withDatabaseName("sitfai_tenant_e2e")
            .withUsername("sitfai")
            .withPassword("sitfaipass");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private BodegaRepository bodegaRepository;

    @Test
    void enforcesTenantIsolationAcrossHttpApplicationAndRepositoryLayers() throws Exception {
        String globalAdmin = token(null, "SUPER_ADMIN");
        UUID empresaA = createEmpresa(globalAdmin, "90000000001", "Empresa Tenant A");
        UUID empresaB = createEmpresa(globalAdmin, "90000000002", "Empresa Tenant B");

        String tenantAdminA = token(empresaA, "EMPRESA_ADMIN");
        String tenantAdminB = token(empresaB, "EMPRESA_ADMIN");
        String tenantUserB = token(empresaB, "CAJERO");

        UUID sucursalA = createSucursal(tenantAdminA, empresaA, "A01", "Sucursal A");
        UUID bodegaA = createBodega(tenantAdminA, sucursalA, "BOD-A", "Bodega A");

        mockMvc.perform(get("/sucursales/{sucursalId}/bodegas", sucursalA)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tenantAdminA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bodegaA.toString()))
                .andExpect(jsonPath("$[0].empresaId").value(empresaA.toString()));

        mockMvc.perform(get("/empresas/{empresaId}/sucursales", empresaA)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tenantAdminB)))
                .andExpect(status().isNotFound());

        mockMvc.perform(put("/empresas/{empresaId}/sucursales/{sucursalId}", empresaA, sucursalA)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tenantAdminB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"A02\",\"nombre\":\"Ataque horizontal\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(delete("/empresas/{empresaId}/sucursales/{sucursalId}", empresaA, sucursalA)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tenantAdminB)))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/bodegas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tenantAdminB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sucursalId\":\"" + sucursalA + "\",\"codigo\":\"BOD-X\",\"nombre\":\"Cross tenant\"}"))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/sucursales/{sucursalId}/bodegas", sucursalA)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tenantAdminB)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/sucursales/{sucursalId}/bodegas", sucursalA)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tenantAdminB))
                        .header("X-Empresa-Id", empresaA.toString()))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/iam/usuarios")
                        .queryParam("empresaId", empresaA.toString())
                        .header(HttpHeaders.AUTHORIZATION, bearer(tenantAdminB)))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/bodegas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tenantUserB))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sucursalId\":\"" + sucursalA + "\",\"codigo\":\"BOD-Y\",\"nombre\":\"Sin rol\"}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/sucursales/{sucursalId}/bodegas", sucursalA)
                        .header(HttpHeaders.AUTHORIZATION, bearer(globalAdmin))
                        .header("X-Empresa-Id", empresaA.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(bodegaA.toString()));

        assertThat(bodegaRepository.buscarPorId(
                BodegaId.de(bodegaA),
                EmpresaId.de(empresaB)))
                .isEmpty();

        mockMvc.perform(put("/empresas/{empresaId}/sucursales/{sucursalId}", empresaA, sucursalA)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tenantAdminA))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"A02\",\"nombre\":\"Sucursal A actualizada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value("A02"));
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

    private UUID createSucursal(String token, UUID empresaId, String codigo, String nombre) throws Exception {
        MvcResult result = mockMvc.perform(post("/empresas/{empresaId}/sucursales", empresaId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"codigo\":\"" + codigo + "\",\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(json(result).get("id").asText());
    }

    private UUID createBodega(String token, UUID sucursalId, String codigo, String nombre) throws Exception {
        MvcResult result = mockMvc.perform(post("/bodegas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sucursalId\":\"" + sucursalId + "\",\"codigo\":\"" + codigo + "\",\"nombre\":\"" + nombre + "\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        return UUID.fromString(json(result).get("id").asText());
    }

    private JsonNode json(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsByteArray());
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String token(UUID empresaId, String... roles) {
        Instant now = Instant.now();
        JwtClaimsSet.Builder claims = JwtClaimsSet.builder()
                .issuer("https://keycloak.test/realms/sitfai-erp")
                .subject(UUID.randomUUID().toString())
                .issuedAt(now)
                .expiresAt(now.plus(10, ChronoUnit.MINUTES))
                .claim("preferred_username", "tenant-e2e")
                .claim("realm_access", Map.of("roles", List.of(roles)));
        if (empresaId != null) {
            claims.claim("empresa_id", empresaId.toString());
        }
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims.build())).getTokenValue();
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class SignedJwtTestConfiguration {

        @Bean
        KeyPair tenantIsolationKeyPair() throws Exception {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        }

        @Bean
        JwtDecoder jwtDecoder(KeyPair tenantIsolationKeyPair) {
            return NimbusJwtDecoder.withPublicKey((RSAPublicKey) tenantIsolationKeyPair.getPublic()).build();
        }

        @Bean
        JwtEncoder jwtEncoder(KeyPair tenantIsolationKeyPair) {
            RSAKey rsaKey = new RSAKey.Builder((RSAPublicKey) tenantIsolationKeyPair.getPublic())
                    .privateKey((RSAPrivateKey) tenantIsolationKeyPair.getPrivate())
                    .keyID("tenant-isolation-e2e")
                    .build();
            return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(rsaKey)));
        }
    }
}
