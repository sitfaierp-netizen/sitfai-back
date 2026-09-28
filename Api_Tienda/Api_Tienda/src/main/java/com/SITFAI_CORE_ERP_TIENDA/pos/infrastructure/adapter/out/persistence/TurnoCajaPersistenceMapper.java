package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.ArqueoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.TransaccionCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.*;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.entity.TransaccionCajaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.entity.TurnoCajaJpaEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Mapper bidireccional entre el Agregado de Dominio TurnoCaja y la Entidad JPA TurnoCajaJpaEntity.
 */
public final class TurnoCajaPersistenceMapper {

    private TurnoCajaPersistenceMapper() {
    }

    // ==========================================
    // Mappings para el Agregado Canónico de Turno
    // ==========================================

    public static TurnoCajaJpaEntity toJpaEntity(TurnoCaja domain) {
        if (domain == null) {
            return null;
        }

        TurnoCajaJpaEntity entity = new TurnoCajaJpaEntity();
        entity.setId(domain.getId().value().toString());
        entity.setEmpresaId(domain.getEmpresaId().value().toString());
        entity.setCajaId(domain.getCajaId().value().toString());
        entity.setCajeroId(domain.getCajeroId().value().toString());
        entity.setCajeroId(domain.getCajeroId().value().toString());
        entity.setEstado(domain.getEstado().name());
        entity.setMontoApertura(domain.getMontoApertura().monto());
        entity.setFechaApertura(domain.getFechaApertura());
        entity.setFechaCierre(domain.getFechaCierre());
        entity.setVersion(domain.getVersion());

        if (domain.getMontoCierre() != null) {
            entity.setMontoCierre(domain.getMontoCierre().monto());
            entity.setMontoDeclarado(domain.getMontoCierre().monto());
        }

        if (domain.getArqueo() != null) {
            ArqueoCaja arqueo = domain.getArqueo();
            entity.setTotalTeorico(arqueo.totalTeoricoEsperado().monto());
            entity.setMontoEsperado(arqueo.totalTeoricoEsperado().monto());
            entity.setMontoDeclarado(arqueo.montoFisicoDeclarado().monto());
            entity.setDescuadre(arqueo.descuadre().monto());
            entity.setDiferencia(arqueo.descuadre().monto());
            entity.setFechaCierre(arqueo.fechaCierre());
        }

        if (domain.getTransacciones() != null) {
            List<TransaccionCajaJpaEntity> txEntities = domain.getTransacciones().stream()
                    .map(tx -> {
                        TransaccionCajaJpaEntity txEntity = new TransaccionCajaJpaEntity();
                        txEntity.setId(tx.getId().toString());
                        txEntity.setTurno(entity);
                        txEntity.setEmpresaId(domain.getEmpresaId().value().toString());
                        txEntity.setTipo(tx.getTipo().name());
                        txEntity.setMonto(tx.getMonto().monto());
                        txEntity.setDocumentoFuenteId(tx.getDocumentoFuenteId());
                        txEntity.setReferencia(tx.getDocumentoFuenteId());
                        txEntity.setFecha(tx.getFechaHora());
                        txEntity.setFechaHora(tx.getFechaHora());
                        return txEntity;
                    }).collect(Collectors.toList());
            entity.setTransacciones(txEntities);
        }

        return entity;
    }

    public static TurnoCaja toDomainEntity(TurnoCajaJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        TurnoId turnoId = TurnoId.fromString(entity.getId());
        EmpresaId empresaId = EmpresaId.fromString(entity.getEmpresaId());
        CajaId cajaId = CajaId.fromString(entity.getCajaId());
        String cajeroStr = (entity.getCajeroId() != null) ? entity.getCajeroId() : entity.getCajeroId();
        CajeroId cajeroId = (cajeroStr != null) ? CajeroId.fromString(cajeroStr) : CajeroId.generar();
        EstadoTurno estado = EstadoTurno.valueOf(entity.getEstado());
        Dinero montoApertura = Dinero.de(entity.getMontoApertura());
        Dinero montoCierre = (entity.getMontoCierre() != null)
                ? Dinero.de(entity.getMontoCierre())
                : ((entity.getMontoDeclarado() != null) ? Dinero.de(entity.getMontoDeclarado()) : null);

        Instant fechaApertura = (entity.getFechaApertura() != null)
                ? entity.getFechaApertura()
                : ((entity.getCreadoEn() != null) ? entity.getCreadoEn() : Instant.now());
        Instant fechaCierre = entity.getFechaCierre();

        List<TransaccionCaja> transacciones = new ArrayList<>();
        if (entity.getTransacciones() != null) {
            for (TransaccionCajaJpaEntity txEntity : entity.getTransacciones()) {
                UUID txId = UUID.fromString(txEntity.getId());
                TipoTransaccionCaja tipo = TipoTransaccionCaja.valueOf(txEntity.getTipo());
                Dinero monto = Dinero.de(txEntity.getMonto());
                String docFuente = (txEntity.getDocumentoFuenteId() != null)
                        ? txEntity.getDocumentoFuenteId()
                        : ((txEntity.getReferencia() != null) ? txEntity.getReferencia() : "SIN_REF");
                Instant txFecha = (txEntity.getFechaHora() != null)
                        ? txEntity.getFechaHora()
                        : ((txEntity.getFecha() != null) ? txEntity.getFecha() : Instant.now());

                transacciones.add(TransaccionCaja.reconstituir(txId, tipo, monto, docFuente, txFecha));
            }
        }

        ArqueoCaja arqueo = null;
        BigDecimal teorico = (entity.getTotalTeorico() != null) ? entity.getTotalTeorico() : entity.getMontoEsperado();
        BigDecimal declarado = (entity.getMontoDeclarado() != null) ? entity.getMontoDeclarado() : entity.getMontoCierre();
        BigDecimal descuadre = (entity.getDescuadre() != null) ? entity.getDescuadre() : entity.getDiferencia();

        if (teorico != null && declarado != null && descuadre != null && fechaCierre != null) {
            Dinero totalVentas = Dinero.cero();
            Dinero totalIngresos = Dinero.cero();
            Dinero totalDevoluciones = Dinero.cero();
            Dinero totalEgresos = Dinero.cero();

            for (TransaccionCaja tx : transacciones) {
                switch (tx.getTipo()) {
                    case VENTA -> totalVentas = totalVentas.sumar(tx.getMonto());
                    case INGRESO -> totalIngresos = totalIngresos.sumar(tx.getMonto());
                    case DEVOLUCION -> totalDevoluciones = totalDevoluciones.sumar(tx.getMonto());
                    case EGRESO -> totalEgresos = totalEgresos.sumar(tx.getMonto());
                }
            }

            arqueo = new ArqueoCaja(
                    montoApertura,
                    totalVentas,
                    totalIngresos,
                    totalDevoluciones,
                    totalEgresos,
                    Dinero.de(teorico),
                    Dinero.de(declarado),
                    Dinero.de(descuadre),
                    fechaCierre
            );
        }

        TurnoCaja turno = TurnoCaja.reconstituir(
                turnoId,
                empresaId,
                cajaId,
                cajeroId,
                estado,
                montoApertura,
                montoCierre,
                transacciones,
                arqueo,
                fechaApertura,
                fechaCierre,
                entity.getVersion()
        );
        return turno;
    }

}
