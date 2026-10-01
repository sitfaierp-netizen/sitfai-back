package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Infraestructura Seguridad: KeycloakJwtAuthenticationConverter (Shared Kernel)")
class KeycloakJwtAuthenticationConverterTest {

    private KeycloakJwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new KeycloakJwtAuthenticationConverter();
    }

    private Jwt createMockJwt(Map<String, Object> claims) {
        return new Jwt(
                "mock-token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                claims
        );
    }

    @Nested
    @DisplayName("Extracción de Roles y Autoridades (Keycloak)")
    class ExtraccionRolesTests {

        @Test
        @DisplayName("Debe extraer roles de realm_access.roles y mapear con prefijo ROLE_")
        void debeMapearRolesDeRealmCorrectamente() {
            Map<String, Object> claims = Map.of(
                    "sub", "user-123",
                    "preferred_username", "operario_caja",
                    "realm_access", Map.of("roles", List.of("CAJERO", "ROLE_BODEGA_OPERATOR", "EMPRESA_ADMIN"))
            );
            Jwt jwt = createMockJwt(claims);

            Collection<GrantedAuthority> authorities = converter.extractAuthorities(jwt);

            List<String> authNames = authorities.stream().map(GrantedAuthority::getAuthority).toList();
            assertThat(authNames).containsExactlyInAnyOrder(
                    "ROLE_CAJERO",
                    "ROLE_BODEGA_OPERATOR",
                    "ROLE_EMPRESA_ADMIN"
            );
        }

        @Test
        @DisplayName("Debe conservar scopes estándar de OAuth2 junto a los roles de Keycloak")
        void debeConservarScopesEstandar() {
            Map<String, Object> claims = Map.of(
                    "sub", "user-123",
                    "scope", "read write openid",
                    "realm_access", Map.of("roles", List.of("SUPER_ADMIN"))
            );
            Jwt jwt = createMockJwt(claims);

            Collection<GrantedAuthority> authorities = converter.extractAuthorities(jwt);

            List<String> authNames = authorities.stream().map(GrantedAuthority::getAuthority).toList();
            assertThat(authNames).contains(
                    "SCOPE_read",
                    "SCOPE_write",
                    "SCOPE_openid",
                    "ROLE_SUPER_ADMIN"
            );
        }

        @Test
        @DisplayName("Debe ignorar roles de realm no administrados por SITFAI")
        void debeIgnorarRolesNoAdministrados() {
            Jwt jwt = createMockJwt(Map.of(
                    "sub", "user-123",
                    "realm_access", Map.of("roles", List.of(
                            "default-roles-sitfai-erp", "offline_access", "realm-admin", "CAJERO"))
            ));

            List<String> authNames = converter.extractAuthorities(jwt).stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            assertThat(authNames).containsExactly("ROLE_CAJERO");
        }
    }

    @Nested
    @DisplayName("Extracción de Contexto Multitenant (MT-01, MT-06)")
    class MultitenancyClaimsTests {

        @Test
        @DisplayName("Debe extraer claim empresa_id y almacenarlo en TenantAuthenticationDetails")
        void debeExtraerEmpresaIdEnAuthenticationDetails() {
            String empresaUuid = UUID.randomUUID().toString();
            Map<String, Object> claims = Map.of(
                    "sub", "user-456",
                    "preferred_username", "admin_empresa",
                    "empresa_id", empresaUuid,
                    "realm_access", Map.of("roles", List.of("EMPRESA_ADMIN"))
            );
            Jwt jwt = createMockJwt(claims);

            AbstractAuthenticationToken token = converter.convert(jwt);

            assertThat(token).isInstanceOf(JwtAuthenticationToken.class);
            assertThat(token.getName()).isEqualTo("admin_empresa");
            assertThat(token.getDetails()).isInstanceOf(TenantAuthenticationDetails.class);

            TenantAuthenticationDetails details = (TenantAuthenticationDetails) token.getDetails();
            assertThat(details.empresaId()).isEqualTo(empresaUuid);
            assertThat(details.empresaUuid()).isEqualTo(UUID.fromString(empresaUuid));
        }

        @Test
        @DisplayName("Debe soportar claim alternativo empresaId si empresa_id no está presente")
        void debeSoportarClaimAlternativoEmpresaId() {
            String empresaUuid = UUID.randomUUID().toString();
            Map<String, Object> claims = Map.of(
                    "sub", "user-789",
                    "empresaId", empresaUuid
            );
            Jwt jwt = createMockJwt(claims);

            AbstractAuthenticationToken token = converter.convert(jwt);

            assertThat(token.getDetails()).isInstanceOf(TenantAuthenticationDetails.class);
            TenantAuthenticationDetails details = (TenantAuthenticationDetails) token.getDetails();
            assertThat(details.empresaId()).isEqualTo(empresaUuid);
        }

        @Test
        @DisplayName("Si no contiene claim de empresa, details queda null pero el token se genera")
        void tokenValidoSinTenantDetails() {
            Map<String, Object> claims = Map.of(
                    "sub", "superadmin-001",
                    "preferred_username", "root_admin"
            );
            Jwt jwt = createMockJwt(claims);

            AbstractAuthenticationToken token = converter.convert(jwt);

            assertThat(token.getDetails()).isNull();
            assertThat(token.getName()).isEqualTo("root_admin");
        }
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException si el JWT es null")
    void debeFallarSiJwtEsNull() {
        assertThrows(NullPointerException.class, () -> converter.convert(null));
    }
}
