package com.SITFAI_CORE_ERP_TIENDA.shared.infrastructure.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación para inyectar de forma criptográficamente segura el identificador de Tenant (EmpresaId)
 * extraído directamente del token JWT de Keycloak (MT-01, MT-06, REGLA 4).
 * <p>
 * Reemplaza el uso de {@code @RequestHeader("X-Empresa-Id")} en los REST Controllers, garantizando
 * el principio de Zero Trust.
 * <p>
 * Tipos de parámetro soportados:
 * <ul>
 *   <li>{@link java.util.UUID} (Recomendado)</li>
 *   <li>{@link String}</li>
 * </ul>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantId {

    /**
     * Indica si el tenant es requerido obligatoriamente para la ejecución del endpoint.
     * Por defecto {@code true}.
     */
    boolean required() default true;
}
