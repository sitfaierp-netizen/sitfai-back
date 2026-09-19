package com.SITFAI_CORE_ERP_TIENDA.pos.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.event.TurnoCerradoEvent;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.SucursalId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.TurnoId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.UsuarioId;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.ArqueoCaja;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TurnoCaja {

    private final TurnoId id;
    private final EmpresaId empresaId;
    private final CajaId cajaId;
    private final SucursalId sucursalId;
    private final UsuarioId usuarioId;
    private EstadoTurno estado;
    private final Dinero montoApertura;
    private final List<TransaccionCaja> transacciones;
    private ArqueoCaja arqueo;
    private final List<DomainEvent> domainEvents;

    private TurnoCaja(TurnoId id, EmpresaId empresaId, CajaId cajaId, SucursalId sucursalId, UsuarioId usuarioId, Dinero montoApertura) {
        this.id = Objects.requireNonNull(id, "TurnoId es obligatorio");
        this.empresaId = Objects.requireNonNull(empresaId, "EmpresaId es obligatorio");
        this.cajaId = Objects.requireNonNull(cajaId, "CajaId es obligatorio");
        this.sucursalId = Objects.requireNonNull(sucursalId, "SucursalId es obligatorio");
        this.usuarioId = Objects.requireNonNull(usuarioId, "UsuarioId es obligatorio");
        this.montoApertura = Objects.requireNonNull(montoApertura, "El monto de apertura es obligatorio");
        this.estado = EstadoTurno.ABIERTO;
        this.transacciones = new ArrayList<>();
        this.domainEvents = new ArrayList<>();
    }

    public static TurnoCaja abrirTurno(EmpresaId empresaId, CajaId cajaId, SucursalId sucursalId, UsuarioId usuarioId, Dinero montoApertura) {
        return new TurnoCaja(TurnoId.generar(), empresaId, cajaId, sucursalId, usuarioId, montoApertura);
    }

    public static TurnoCaja reconstituir(TurnoId id, EmpresaId empresaId, CajaId cajaId, SucursalId sucursalId, UsuarioId usuarioId, EstadoTurno estado, Dinero montoApertura, List<TransaccionCaja> transacciones, ArqueoCaja arqueo) {
        TurnoCaja turno = new TurnoCaja(id, empresaId, cajaId, sucursalId, usuarioId, montoApertura);
        turno.estado = estado;
        if (transacciones != null) {
            turno.transacciones.addAll(transacciones);
        }
        turno.arqueo = arqueo;
        return turno;
    }

    public void registrarTransaccion(TipoTransaccion tipo, Dinero monto, String referencia) {
        Objects.requireNonNull(tipo, "El tipo de transacción es obligatorio");
        Objects.requireNonNull(monto, "El monto es obligatorio");
        
        if (this.estado != EstadoTurno.ABIERTO) {
            throw new IllegalStateException("No se pueden registrar transacciones porque el turno está " + this.estado);
        }

        TransaccionCaja transaccion = TransaccionCaja.registrar(tipo, monto, referencia);
        this.transacciones.add(transaccion);
    }

    public Dinero calcularConsolidado() {
        Dinero consolidado = this.montoApertura;
        
        for (TransaccionCaja transaccion : this.transacciones) {
            if (transaccion.getTipo() == TipoTransaccion.VENTA || transaccion.getTipo() == TipoTransaccion.INGRESO) {
                consolidado = consolidado.sumar(transaccion.getMonto());
            } else if (transaccion.getTipo() == TipoTransaccion.DEVOLUCION || transaccion.getTipo() == TipoTransaccion.EGRESO) {
                consolidado = consolidado.restar(transaccion.getMonto());
            }
        }
        
        return consolidado;
    }

    public void cerrarTurno(Dinero montoDeclarado) {
        Objects.requireNonNull(montoDeclarado, "El monto declarado es obligatorio para cerrar el turno");
        
        if (this.estado != EstadoTurno.ABIERTO) {
            throw new IllegalStateException("El turno ya se encuentra " + this.estado);
        }

        Dinero totalVentas = Dinero.cero();
        Dinero totalDevoluciones = Dinero.cero();
        Dinero totalIngresos = Dinero.cero();
        Dinero totalEgresos = Dinero.cero();

        for (TransaccionCaja transaccion : this.transacciones) {
            switch (transaccion.getTipo()) {
                case VENTA -> totalVentas = totalVentas.sumar(transaccion.getMonto());
                case DEVOLUCION -> totalDevoluciones = totalDevoluciones.sumar(transaccion.getMonto());
                case INGRESO -> totalIngresos = totalIngresos.sumar(transaccion.getMonto());
                case EGRESO -> totalEgresos = totalEgresos.sumar(transaccion.getMonto());
            }
        }

        this.arqueo = ArqueoCaja.calcular(
                this.montoApertura,
                totalVentas,
                totalDevoluciones,
                totalIngresos,
                totalEgresos,
                montoDeclarado
        );

        this.estado = EstadoTurno.CERRADO;
        
        this.domainEvents.add(TurnoCerradoEvent.of(this.id, this.empresaId, this.cajaId, this.usuarioId, this.arqueo.balanceEsperado(), this.arqueo.montoDeclarado(), this.arqueo.diferencia()));
    }

    // Getters
    public TurnoId getId() {
        return id;
    }

    public EmpresaId getEmpresaId() {
        return empresaId;
    }

    public CajaId getCajaId() {
        return cajaId;
    }

    public SucursalId getSucursalId() {
        return sucursalId;
    }

    public UsuarioId getUsuarioId() {
        return usuarioId;
    }

    public EstadoTurno getEstado() {
        return estado;
    }

    public Dinero getMontoApertura() {
        return montoApertura;
    }

    public List<TransaccionCaja> getTransacciones() {
        return Collections.unmodifiableList(transacciones);
    }

    public ArqueoCaja getArqueo() {
        return arqueo;
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
}
