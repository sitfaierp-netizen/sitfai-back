package com.SITFAI_CORE_ERP_TIENDA.billing.domain.model.factura.vo;

import java.util.Objects;

/**
 * Value Object: Referencia al Documento Fuente que origina la Factura
 * (ej. el Ticket de venta del POS o la Orden de Venta / Pedido del E-commerce).
 * <p>
 * Garantiza la trazabilidad documental (AUD-04) sin acoplamiento estructural entre Bounded Contexts.
 * Inmutable (record Java 21).
 */
public record DocumentoFuenteId(String tipo, String numero) {

    public DocumentoFuenteId {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("DocumentoFuenteId: el tipo no puede ser nulo ni vacío.");
        }
        if (numero == null || numero.isBlank()) {
            throw new IllegalArgumentException("DocumentoFuenteId: el número no puede ser nulo ni vacío.");
        }
    }

    public static DocumentoFuenteId de(String tipo, String numero) {
        return new DocumentoFuenteId(tipo, numero);
    }

    public static DocumentoFuenteId pos(String ticketId) {
        return new DocumentoFuenteId("TICKET_POS", ticketId);
    }

    public static DocumentoFuenteId ecommerce(String pedidoId) {
        return new DocumentoFuenteId("PEDIDO_ECOMMERCE", pedidoId);
    }

    @Override
    public String toString() {
        return tipo + "#" + numero;
    }
}
