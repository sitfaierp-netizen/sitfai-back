package com.SITFAI_CORE_ERP_TIENDA.billing.domain.exception;

/**
 * Excepción de regla de negocio del dominio Billing (concreta).
 */
public class BillingRuleException extends DomainException {
    public BillingRuleException(String mensaje) {
        super(mensaje);
    }
    public BillingRuleException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
