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
        entity.setUsuarioId(domain.getCajeroId().value().toString());
        entity.setEstado(domain.getEstado().name());
        entity.setMontoApertura(domain.getMontoApertura().monto());
        entity.setFechaApertura(domain.getFechaApertura());
        entity.setFechaCierre(domain.getFechaCierre());

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
        String cajeroStr = (entity.getCajeroId() != null) ? entity.getCajeroId() : entity.getUsuarioId();
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

        return TurnoCaja.reconstituir(
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
                fechaCierre
        );
    }

    // ==========================================
    // Mappings para compatibilidad con componentes legados
    // ==========================================

    public static TurnoCajaJpaEntity toJpaEntityLegacy(com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja domain) {
        if (domain == null) return null;

        TurnoCajaJpaEntity entity = new TurnoCajaJpaEntity();
        entity.setId(domain.getId().value().toString());
        entity.setEmpresaId(domain.getEmpresaId().value().toString());
        entity.setCajaId(domain.getCajaId().value().toString());
        entity.setSucursalId(domain.getSucursalId().value().toString());
        entity.setUsuarioId(domain.getUsuarioId().value().toString());
        entity.setCajeroId(domain.getUsuarioId().value().toString());
        entity.setEstado(domain.getEstado().name());
        entity.setMontoApertura(domain.getMontoApertura().valor());

        if (domain.getArqueo() != null) {
            entity.setMontoEsperado(domain.getArqueo().balanceEsperado().valor());
            entity.setTotalTeorico(domain.getArqueo().balanceEsperado().valor());
            entity.setMontoDeclarado(domain.getArqueo().montoDeclarado().valor());
            entity.setMontoCierre(domain.getArqueo().montoDeclarado().valor());
            entity.setDiferencia(domain.getArqueo().diferencia().valor());
            entity.setDescuadre(domain.getArqueo().diferencia().valor());
        }

        List<TransaccionCajaJpaEntity> transaccionesEntity = domain.getTransacciones().stream()
                .map(tx -> {
                    TransaccionCajaJpaEntity txEntity = new TransaccionCajaJpaEntity();
                    txEntity.setId(tx.getId().toString());
                    txEntity.setTurno(entity);
                    txEntity.setEmpresaId(domain.getEmpresaId().value().toString());
                    txEntity.setTipo(tx.getTipo().name());
                    txEntity.setMonto(tx.getMonto().valor());
                    txEntity.setReferencia(tx.getReferencia());
                    txEntity.setDocumentoFuenteId(tx.getReferencia());
                    txEntity.setFecha(tx.getFecha());
                    txEntity.setFechaHora(tx.getFecha());
                    return txEntity;
                }).collect(Collectors.toList());

        entity.setTransacciones(transaccionesEntity);
        return entity;
    }

    public static com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja toLegacyDomainEntity(TurnoCajaJpaEntity entity) {
        if (entity == null) return null;

        var turnoId = new com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.TurnoId(UUID.fromString(entity.getId()));
        var empresaId = new com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId(UUID.fromString(entity.getEmpresaId()));
        var cajaId = new com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.CajaId(UUID.fromString(entity.getCajaId()));
        var sucursalId = new com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.SucursalId(entity.getSucursalId() != null ? UUID.fromString(entity.getSucursalId()) : UUID.randomUUID());
        var usuarioId = new com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.UsuarioId(entity.getUsuarioId() != null ? UUID.fromString(entity.getUsuarioId()) : (entity.getCajeroId() != null ? UUID.fromString(entity.getCajeroId()) : UUID.randomUUID()));
        var estado = com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.EstadoTurno.valueOf(entity.getEstado());
        var montoApertura = com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero.de(entity.getMontoApertura());

        List<com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TransaccionCaja> txList = new ArrayList<>();
        if (entity.getTransacciones() != null) {
            for (TransaccionCajaJpaEntity txEntity : entity.getTransacciones()) {
                var tipo = com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TipoTransaccion.valueOf(txEntity.getTipo());
                var monto = com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero.de(txEntity.getMonto());
                txList.add(com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TransaccionCaja.reconstituir(
                        UUID.fromString(txEntity.getId()),
                        tipo,
                        monto,
                        txEntity.getReferencia() != null ? txEntity.getReferencia() : txEntity.getDocumentoFuenteId(),
                        txEntity.getFecha() != null ? txEntity.getFecha() : Instant.now()
                ));
            }
        }

        com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.ArqueoCaja arqueo = null;
        BigDecimal esperado = entity.getMontoEsperado() != null ? entity.getMontoEsperado() : entity.getTotalTeorico();
        BigDecimal declarado = entity.getMontoDeclarado() != null ? entity.getMontoDeclarado() : entity.getMontoCierre();
        BigDecimal dif = entity.getDiferencia() != null ? entity.getDiferencia() : entity.getDescuadre();

        if (esperado != null && declarado != null && dif != null) {
            arqueo = new com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.ArqueoCaja(
                    montoApertura,
                    com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero.cero(),
                    com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero.cero(),
                    com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero.cero(),
                    com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero.cero(),
                    com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero.de(esperado),
                    com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero.de(declarado),
                    com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero.de(dif)
            );
        }

        return com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja.reconstituir(
                turnoId, empresaId, cajaId, sucursalId, usuarioId, estado, montoApertura, txList, arqueo
        );
    }
}
