package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web;

import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security.KeycloakJwtAuthenticationConverter;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security.TenantAuthenticationDetails;
import org.springframework.core.MethodParameter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

/**
 * Argument Resolver para inyectar de forma segura el identificador de Tenant (EmpresaId)
 * extraído directamente del contexto criptográfico de Spring Security (JWT de Keycloak).
 * <p>
 * Reglas validadas:
 * <ul>
 *   <li>REGLA 4: El tenant proviene del JWT validado, nunca de cabeceras arbitrarias del cliente.</li>
 *   <li>REGLA 7: Validación de claims de Keycloak IAM (OAuth2 Resource Server).</li>
 *   <li>MT-01 / MT-06: Extracción estricta del claim {@code empresa_id}.</li>
 * </ul>
 */
@Component
public class TenantIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(TenantId.class);
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        TenantId tenantIdAnnotation = parameter.getParameterAnnotation(TenantId.class);
        boolean required = tenantIdAnnotation == null || tenantIdAnnotation.required();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            if (required) {
                throw new AuthenticationCredentialsNotFoundException(
                        "No se encontró un contexto de autenticación válido para resolver @TenantId.");
            }
            return null;
        }

        String empresaIdStr = extractEmpresaIdFromAuthentication(authentication);

        if (empresaIdStr == null || empresaIdStr.isBlank()) {
            boolean isSuperAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
            if (isSuperAdmin) {
                empresaIdStr = webRequest.getHeader("X-Empresa-Id");
            }
            if (empresaIdStr == null || empresaIdStr.isBlank()) {
                if (required) {
                    throw new AccessDeniedException(
                            "El token JWT de autenticación no contiene el claim obligatorio 'empresa_id' (MT-01, MT-06) y no se proveyó X-Empresa-Id como SUPER_ADMIN.");
                }
                return null;
            }
        }

        Class<?> paramType = parameter.getParameterType();
        if (UUID.class.isAssignableFrom(paramType)) {
            try {
                return UUID.fromString(empresaIdStr.trim());
            } catch (IllegalArgumentException e) {
                throw new AccessDeniedException(
                        String.format("El claim 'empresa_id' en el JWT ('%s') no es un formato UUID válido.", empresaIdStr), e);
            }
        } else if (String.class.isAssignableFrom(paramType)) {
            return empresaIdStr.trim();
        }

        throw new IllegalArgumentException(
                String.format("Tipo de parámetro no soportado para @TenantId: '%s'. Se requiere java.util.UUID o String.",
                        paramType.getName())
        );
    }

    /**
     * Extrae el valor del tenant desde los detalles de autenticación o los claims del JWT.
     */
    private String extractEmpresaIdFromAuthentication(Authentication authentication) {
        // 1. Intentar desde TenantAuthenticationDetails si fue asignado por KeycloakJwtAuthenticationConverter
        if (authentication.getDetails() instanceof TenantAuthenticationDetails tenantDetails) {
            return tenantDetails.empresaId();
        }

        // 2. Intentar directamente desde JwtAuthenticationToken
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return extractFromJwt(jwt);
        }

        // 3. Intentar desde el Principal si es un Jwt
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return extractFromJwt(jwt);
        }

        return null;
    }

    private String extractFromJwt(Jwt jwt) {
        if (jwt == null) {
            return null;
        }
        String claim = jwt.getClaimAsString(KeycloakJwtAuthenticationConverter.CLAIM_EMPRESA_ID);
        if (claim == null || claim.isBlank()) {
            claim = jwt.getClaimAsString(KeycloakJwtAuthenticationConverter.CLAIM_EMPRESA_ID_ALT);
        }
        return claim;
    }
}
