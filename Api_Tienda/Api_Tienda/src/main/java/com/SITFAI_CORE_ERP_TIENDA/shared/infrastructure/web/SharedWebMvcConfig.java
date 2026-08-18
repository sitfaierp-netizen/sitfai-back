package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Objects;

/**
 * Configuración de Spring WebMvc para el Shared Kernel.
 * Registra el {@link TenantIdArgumentResolver} para permitir la inyección segura de {@code @TenantId}
 * en los controladores REST de todos los Bounded Contexts.
 */
@Configuration
public class SharedWebMvcConfig implements WebMvcConfigurer {

    private final TenantIdArgumentResolver tenantIdArgumentResolver;

    public SharedWebMvcConfig(TenantIdArgumentResolver tenantIdArgumentResolver) {
        this.tenantIdArgumentResolver = Objects.requireNonNull(tenantIdArgumentResolver, "tenantIdArgumentResolver no puede ser null");
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(tenantIdArgumentResolver);
    }
}
