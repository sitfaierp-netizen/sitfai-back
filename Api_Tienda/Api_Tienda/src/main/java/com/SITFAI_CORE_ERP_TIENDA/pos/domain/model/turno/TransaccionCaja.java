package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TipoTransaccionCaja;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad interna protegida del Agregado TurnoCaja.
 * <p>
 * Representa un movimiento financiero ocurrido dentro de la sesión de caja.
 * Inmutable post-registro (Regla DOC-01).
 * Exige un documento fuente traceable (Regla BOD-04).
 */
public class TransaccionCaja {

    private final UUID id;
    private final TipoTransaccionCaja tipo;
    private final Dinero monto;
    private final String documentoFuenteId;
    private final Instant fechaHora;

    protected TransaccionCaja(
            UUID id,
            TipoTransaccionCaja tipo,
            Dinero monto,
            String documentoFuenteId,
            Instant fechaHora
    ) {
        this.id = Objects.requireNonNull(id, "El ID de la transacción no puede ser nulo");
        this.tipo = Objects.requireNonNull(tipo, "El tipo de transacción es obligatorio");
        this.monto = Objects.requireNonNull(monto, "El monto de la transacción es obligatorio");
        if (monto.esNegativo() || monto.esCero()) {
            throw new IllegalArgumentException("El monto de la transacción debe ser mayor a cero: " + monto);
        }
        this.documentoFuenteId = Objects.requireNonNull(documentoFuenteId, "El documento fuente es obligatorio (BOD-04)");
        this.fechaHora = Objects.requireNonNull(fechaHora, "La fecha y hora de la transacción es obligatoria");
    }

    public static TransaccionCaja registrar(TipoTransaccionCaja tipo, Dinero monto, String documentoFuenteId) {
        return new TransaccionCaja(UUID.randomUUID(), tipo, monto, documentoFuenteId, Instant.now());
    }

    public static TransaccionCaja reconstituir(
            UUID id,
            TipoTransaccionCaja tipo,
            Dinero monto,
            String documentoFuenteId,
            Instant fechaHora
    ) {
        return new TransaccionCaja(id, tipo, monto, documentoFuenteId, fechaHora);
    }

    public UUID getId() {
        return id;
    }

    public TipoTransaccionCaja getTipo() {
        return tipo;
    }

    public Dinero getMonto() {
        return monto;
    }

    public String getDocumentoFuenteId() {
        return documentoFuenteId;
    }

    public Instant getFechaHora() {
        return fechaHora;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransaccionCaja that = (TransaccionCaja) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "TransaccionCaja{" +
                "id=" + id +
                ", tipo=" + tipo +
                ", monto=" + monto +
                ", documentoFuenteId='" + documentoFuenteId + '\'' +
                ", fechaHora=" + fechaHora +
                '}';
    }
}
