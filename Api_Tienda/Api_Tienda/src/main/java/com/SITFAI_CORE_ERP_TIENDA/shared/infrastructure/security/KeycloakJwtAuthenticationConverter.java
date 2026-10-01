package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Conversor de JWT emitido por Keycloak a Spring Security Authentication Token.
 * <p>
 * Responsabilidades:
 * 1. Extrae los roles de Keycloak desde el claim {@code realm_access.roles} y los transforma en {@link GrantedAuthority}
 *    con el prefijo {@code ROLE_} (ej. {@code ROLE_SUPER_ADMIN}, {@code ROLE_EMPRESA_ADMIN}, {@code ROLE_CAJERO}).
 * 2. Mantiene los scopes estándar OAuth2 vía {@link JwtGrantedAuthoritiesConverter}.
 * 3. Extrae el claim personalizado {@code empresa_id} (MT-06) y lo almacena como {@link TenantAuthenticationDetails}
 *    en los detalles de la autenticación.
 * <p>
 * Reglas validadas: REGLA 4 (Multitenancy JWT), REGLA 7 (Seguridad Keycloak), MT-01, MT-06.
 */
@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    public static final String CLAIM_REALM_ACCESS = "realm_access";
    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_EMPRESA_ID = "empresa_id";
    public static final String CLAIM_EMPRESA_ID_ALT = "empresaId";
    public static final String CLAIM_PREFERRED_USERNAME = "preferred_username";
    public static final String ROLE_PREFIX = "ROLE_";
    private static final Set<String> ALLOWED_REALM_ROLES = Set.of(
            "SUPER_ADMIN",
            "EMPRESA_ADMIN",
            "SUCURSAL_MANAGER",
            "BODEGA_OPERATOR",
            "CAJERO"
    );

    private final JwtGrantedAuthoritiesConverter defaultGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Objects.requireNonNull(jwt, "Jwt no puede ser null");

        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        String principalClaimName = jwt.getClaimAsString(CLAIM_PREFERRED_USERNAME);
        if (principalClaimName == null || principalClaimName.isBlank()) {
            principalClaimName = jwt.getSubject();
        }

        JwtAuthenticationToken authenticationToken = new JwtAuthenticationToken(jwt, authorities, principalClaimName);

        // Extraer y almacenar el tenant context en los detalles de autenticación
        String empresaId = extractEmpresaId(jwt);
        if (empresaId != null && !empresaId.isBlank()) {
            authenticationToken.setDetails(new TenantAuthenticationDetails(empresaId.trim()));
        }

        return authenticationToken;
    }

    /**
     * Extrae todas las autoridades concedidas (Scopes de OAuth2 y Roles de Realm de Keycloak).
     */
    public Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // 1. Scopes estándar OAuth2 (SCOPE_*)
        Collection<GrantedAuthority> defaultAuthorities = defaultGrantedAuthoritiesConverter.convert(jwt);
        if (defaultAuthorities != null) {
            authorities.addAll(defaultAuthorities);
        }

        // 2. Roles del Realm de Keycloak (realm_access.roles)
        Map<String, Object> realmAccess = jwt.getClaimAsMap(CLAIM_REALM_ACCESS);
        if (realmAccess != null && realmAccess.containsKey(CLAIM_ROLES)) {
            Object rolesObj = realmAccess.get(CLAIM_ROLES);
            if (rolesObj instanceof Collection<?> roles) {
                Set<GrantedAuthority> realmRoles = roles.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .map(String::trim)
                        .filter(role -> !role.isBlank())
                        .map(role -> role.startsWith(ROLE_PREFIX) ? role.substring(ROLE_PREFIX.length()) : role)
                        .map(role -> role.toUpperCase(Locale.ROOT))
                        .filter(ALLOWED_REALM_ROLES::contains)
                        .map(role -> ROLE_PREFIX + role)
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());
                authorities.addAll(realmRoles);
            }
        }

        return Collections.unmodifiableSet(authorities);
    }

    /**
     * Extrae el identificador del tenant (empresa_id) desde los claims del JWT.
     */
    public String extractEmpresaId(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        String empresaId = jwt.getClaimAsString(CLAIM_EMPRESA_ID);
        if (empresaId == null || empresaId.isBlank()) {
            empresaId = jwt.getClaimAsString(CLAIM_EMPRESA_ID_ALT);
        }
        return empresaId;
    }
}
