package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.TurnoCerradoEvent;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.CajeroId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EstadoTurno;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TipoTransaccionCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TurnoId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root: TurnoCaja (Control financiero y operativo de caja - Reglas CAJ-02 a CAJ-07).
 * <p>
 * Regla MT-01: Encapsula {@link EmpresaId} como clave inmutable de aislamiento multitenant.
 * Regla DOC-01: Tras la ejecución de {@link #cerrar(Dinero)}, el estado pasa a CERRADO y no
 * admite alteraciones posteriores.
 */
public class TurnoCaja {

    private final TurnoId id;
    private final EmpresaId empresaId;
    private final CajaId cajaId;
    private final CajeroId cajeroId;
    private EstadoTurno estado;
    private final Dinero montoApertura;
    private Dinero montoCierre;
    private final List<TransaccionCaja> transacciones;
    private ArqueoCaja arqueo;
    private final Instant fechaApertura;
    private Instant fechaCierre;
    private Long version;
    private final List<Object> domainEvents;

    private TurnoCaja(
            TurnoId id,
            EmpresaId empresaId,
            CajaId cajaId,
            CajeroId cajeroId,
            Dinero montoApertura,
            Instant fechaApertura
    ) {
        this.id = Objects.requireNonNull(id, "El ID del turno no puede ser nulo");
        this.empresaId = Objects.requireNonNull(empresaId, "El EmpresaId es obligatorio para MT-01");
        this.cajaId = Objects.requireNonNull(cajaId, "El CajaId es obligatorio");
        this.cajeroId = Objects.requireNonNull(cajeroId, "El CajeroId es obligatorio");
        this.montoApertura = Objects.requireNonNull(montoApertura, "El monto de apertura no puede ser nulo");
        if (montoApertura.esNegativo()) {
            throw new IllegalArgumentException("El monto de apertura no puede ser negativo: " + montoApertura);
        }
        this.estado = EstadoTurno.ABIERTO;
        this.montoCierre = null;
        this.transacciones = new ArrayList<>();
        this.arqueo = null;
        this.fechaApertura = Objects.requireNonNull(fechaApertura, "La fecha de apertura no puede ser nula");
        this.fechaCierre = null;
        this.domainEvents = new ArrayList<>();
    }

    /**
     * Factory Method de Dominio: Apertura de Turno en estado inicial ABIERTO (Regla CAJ-02).
     */
    public static TurnoCaja abrir(
            EmpresaId empresaId,
            CajaId cajaId,
            CajeroId cajeroId,
            Dinero montoApertura
    ) {
        return new TurnoCaja(TurnoId.generar(), empresaId, cajaId, cajeroId, montoApertura, Instant.now());
    }

    /**
     * Factory Method de Dominio con ID explícito de turno.
     */
    public static TurnoCaja abrir(
            TurnoId id,
            EmpresaId empresaId,
            CajaId cajaId,
            CajeroId cajeroId,
            Dinero montoApertura
    ) {
        return new TurnoCaja(id, empresaId, cajaId, cajeroId, montoApertura, Instant.now());
    }

    /**
     * Reconstitución del Agregado desde la capa de persistencia/infraestructura.
     */
    public static TurnoCaja reconstituir(
            TurnoId id,
            EmpresaId empresaId,
            CajaId cajaId,
            CajeroId cajeroId,
            EstadoTurno estado,
            Dinero montoApertura,
            Dinero montoCierre,
            List<TransaccionCaja> transacciones,
            ArqueoCaja arqueo,
            Instant fechaApertura,
            Instant fechaCierre,
            Long version
    ) {
        TurnoCaja turno = new TurnoCaja(id, empresaId, cajaId, cajeroId, montoApertura, fechaApertura);
        turno.estado = estado;
        turno.montoCierre = montoCierre;
        if (transacciones != null) {
            turno.transacciones.addAll(transacciones);
        }
        turno.arqueo = arqueo;
        turno.fechaCierre = fechaCierre;
        turno.version = version;
        return turno;
    }

    /**
     * Registra una transacción financiera en el turno (Regla CAJ-03 y CAJ-04).
     * <p>
     * Validación Fail-Fast: Impide cualquier registro si el turno no se encuentra en estado ABIERTO.
     */
    public java.util.UUID registrarTransaccion(TipoTransaccionCaja tipo, Dinero monto, String documentoFuenteId) {
        Objects.requireNonNull(tipo, "El tipo de transacción es obligatorio");
        Objects.requireNonNull(monto, "El monto de la transacción es obligatorio");
        Objects.requireNonNull(documentoFuenteId, "El documento fuente es obligatorio (BOD-04)");

        if (this.estado != EstadoTurno.ABIERTO) {
            throw new IllegalStateException("Fail-fast: No se pueden registrar transacciones porque el turno está " + this.estado);
        }

        if (monto.esCero() || monto.esNegativo()) {
            throw new IllegalArgumentException("El monto de la transacción debe ser mayor a cero: " + monto);
        }

        TransaccionCaja transaccion = TransaccionCaja.registrar(tipo, monto, documentoFuenteId);
        this.transacciones.add(transaccion);
        
        return transaccion.getId();
    }

    /**
     * Cierre transaccional del turno con arqueo financiero inmutable (Reglas CAJ-05 a CAJ-07 y DOC-01).
     *
     * @param montoFisicoDeclarado Dinero contado físicamente en caja por el cajero.
     * @return ArqueoCaja inmutable con el desglose y el descuadre verificado.
     */
    public ArqueoCaja cerrar(Dinero montoFisicoDeclarado) {
        Objects.requireNonNull(montoFisicoDeclarado, "El monto físico declarado es obligatorio para cerrar el turno");
        if (montoFisicoDeclarado.esNegativo()) {
            throw new IllegalArgumentException("El monto físico declarado no puede ser negativo: " + montoFisicoDeclarado);
        }

        if (this.estado != EstadoTurno.ABIERTO) {
            throw new IllegalStateException("El turno ya se encuentra " + this.estado + " y no puede cerrarse nuevamente");
        }

        Dinero totalVentas = Dinero.cero();
        Dinero totalIngresos = Dinero.cero();
        Dinero totalDevoluciones = Dinero.cero();
        Dinero totalEgresos = Dinero.cero();

        for (TransaccionCaja tx : this.transacciones) {
            switch (tx.getTipo()) {
                case VENTA -> totalVentas = totalVentas.sumar(tx.getMonto());
                case INGRESO -> totalIngresos = totalIngresos.sumar(tx.getMonto());
                case DEVOLUCION -> totalDevoluciones = totalDevoluciones.sumar(tx.getMonto());
                case EGRESO -> totalEgresos = totalEgresos.sumar(tx.getMonto());
            }
        }

        Instant ahora = Instant.now();
        this.arqueo = ArqueoCaja.calcular(
                this.montoApertura,
                totalVentas,
                totalIngresos,
                totalDevoluciones,
                totalEgresos,
                montoFisicoDeclarado,
                ahora
        );

        this.montoCierre = montoFisicoDeclarado;
        this.estado = EstadoTurno.CERRADO;
        this.fechaCierre = ahora;

        TurnoCerradoEvent evento = TurnoCerradoEvent.of(
                this.id,
                this.empresaId,
                this.cajaId,
                this.cajeroId,
                this.montoApertura,
                totalVentas,
                totalIngresos,
                totalDevoluciones,
                totalEgresos,
                this.arqueo.totalTeoricoEsperado(),
                montoFisicoDeclarado,
                this.arqueo.descuadre(),
                ahora
        );
        this.domainEvents.add(evento);

        return this.arqueo;
    }

    /**
     * Calcula dinámicamente el total teórico actual según las transacciones registradas hasta el momento.
     */
    public Dinero calcularTotalTeorico() {
        Dinero total = this.montoApertura;
        for (TransaccionCaja tx : this.transacciones) {
            switch (tx.getTipo()) {
                case VENTA, INGRESO -> total = total.sumar(tx.getMonto());
                case DEVOLUCION, EGRESO -> total = total.restar(tx.getMonto());
            }
        }
        return total;
    }

    // Getters y Encapsulamiento
    public TurnoId getId() {
        return id;
    }

    public EmpresaId getEmpresaId() {
        return empresaId;
    }

    public CajaId getCajaId() {
        return cajaId;
    }

    public CajeroId getCajeroId() {
        return cajeroId;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public Dinero getMontoApertura() {
        return montoApertura;
    }

    public Dinero getMontoCierre() {
        return montoCierre;
    }

    public List<TransaccionCaja> getTransacciones() {
        return Collections.unmodifiableList(transacciones);
    }

    public ArqueoCaja getArqueo() {
        return arqueo;
    }

    public Instant getFechaApertura() {
        return fechaApertura;
    }

    public Instant getFechaCierre() {
        return fechaCierre;
    }

    public List<Object> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
