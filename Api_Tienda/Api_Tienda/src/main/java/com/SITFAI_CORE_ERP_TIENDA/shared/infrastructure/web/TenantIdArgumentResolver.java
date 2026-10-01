package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web;

import com.SITFAI_CORE_ERP_TIENDA.shared.application.security.CurrentTenantProvider;
import com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.security.TenantScopeViolationException;
import org.springframework.core.MethodParameter;
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

    private static final String TENANT_HEADER = "X-Empresa-Id";

    private final CurrentTenantProvider currentTenantProvider;

    public TenantIdArgumentResolver(CurrentTenantProvider currentTenantProvider) {
        this.currentTenantProvider = currentTenantProvider;
    }

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

        String requestedTenant = webRequest.getHeader(TENANT_HEADER);
        UUID empresaId;
        try {
            empresaId = requestedTenant == null || requestedTenant.isBlank()
                    ? currentTenantProvider.requireCurrentTenant()
                    : currentTenantProvider.authorizeTenant(UUID.fromString(requestedTenant.trim()));
        } catch (TenantScopeViolationException exception) {
            if (!required) {
                return null;
            }
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new TenantScopeViolationException(exception);
        }

        Class<?> paramType = parameter.getParameterType();
        if (UUID.class.isAssignableFrom(paramType)) {
            return empresaId;
        } else if (String.class.isAssignableFrom(paramType)) {
            return empresaId.toString();
        }

        throw new IllegalArgumentException(
                String.format("Tipo de parámetro no soportado para @TenantId: '%s'. Se requiere java.util.UUID o String.",
                        paramType.getName())
        );
    }

}
