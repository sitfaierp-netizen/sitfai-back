package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class TransaccionCaja {
    private final UUID id;
    private final TipoTransaccion tipo;
    private final Dinero monto;
    private final String referencia;
    private final Instant fecha;

    private TransaccionCaja(UUID id, TipoTransaccion tipo, Dinero monto, String referencia, Instant fecha) {
        this.id = Objects.requireNonNull(id, "El ID de la transacción es obligatorio");
        this.tipo = Objects.requireNonNull(tipo, "El tipo de transacción es obligatorio");
        this.monto = Objects.requireNonNull(monto, "El monto es obligatorio");
        this.referencia = referencia; // Puede ser null
        this.fecha = Objects.requireNonNull(fecha, "La fecha es obligatoria");
    }

    public static TransaccionCaja registrar(TipoTransaccion tipo, Dinero monto, String referencia) {
        return new TransaccionCaja(UUID.randomUUID(), tipo, monto, referencia, Instant.now());
    }

    public static TransaccionCaja reconstituir(UUID id, TipoTransaccion tipo, Dinero monto, String referencia, Instant fecha) {
        return new TransaccionCaja(id, tipo, monto, referencia, fecha);
    }

    public UUID getId() {
        return id;
    }

    public TipoTransaccion getTipo() {
        return tipo;
    }

    public Dinero getMonto() {
        return monto;
    }

    public String getReferencia() {
        return referencia;
    }

    public Instant getFecha() {
        return fecha;
    }
}
