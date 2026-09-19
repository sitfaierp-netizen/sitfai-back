package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.EstadoTurno;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TipoTransaccion;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TransaccionCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.ArqueoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.SucursalId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.TurnoId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.valueobject.UsuarioId;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.entity.TransaccionCajaJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.entity.TurnoCajaJpaEntity;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TurnoCajaPersistenceMapper {

    private TurnoCajaPersistenceMapper() {}

    public static TurnoCajaJpaEntity toJpaEntity(TurnoCaja domain) {
        TurnoCajaJpaEntity entity = new TurnoCajaJpaEntity();
        entity.setId(domain.getId().value().toString());
        entity.setEmpresaId(domain.getEmpresaId().value().toString());
        entity.setCajaId(domain.getCajaId().value().toString());
        entity.setSucursalId(domain.getSucursalId().value().toString());
        entity.setUsuarioId(domain.getUsuarioId().value().toString());
        entity.setEstado(domain.getEstado().name());
        entity.setMontoApertura(domain.getMontoApertura().valor());

        if (domain.getArqueo() != null) {
            entity.setMontoEsperado(domain.getArqueo().balanceEsperado().valor());
            entity.setMontoDeclarado(domain.getArqueo().montoDeclarado().valor());
            entity.setDiferencia(domain.getArqueo().diferencia().valor());
        } else {
            entity.setMontoEsperado(null);
            entity.setMontoDeclarado(null);
            entity.setDiferencia(null);
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
                    txEntity.setFecha(tx.getFecha());
                    return txEntity;
                }).collect(Collectors.toList());

        entity.getTransacciones().addAll(transaccionesEntity);
        return entity;
    }

    public static TurnoCaja toDomainEntity(TurnoCajaJpaEntity entity) {
        try {
            List<TransaccionCaja> transaccionesDomain = entity.getTransacciones().stream()
                    .map(TurnoCajaPersistenceMapper::toTransaccionDomain)
                    .collect(Collectors.toList());

            ArqueoCaja arqueo = null;
            if (entity.getMontoEsperado() != null && entity.getMontoDeclarado() != null) {
                arqueo = new ArqueoCaja(
                        Dinero.de(entity.getMontoApertura()),
                        Dinero.cero(),
                        Dinero.cero(),
                        Dinero.cero(),
                        Dinero.cero(),
                        Dinero.de(entity.getMontoEsperado()),
                        Dinero.de(entity.getMontoDeclarado()),
                        Dinero.de(entity.getDiferencia())
                );
            }

            return TurnoCaja.reconstituir(
                    new TurnoId(UUID.fromString(entity.getId())),
                    new EmpresaId(UUID.fromString(entity.getEmpresaId())),
                    new CajaId(UUID.fromString(entity.getCajaId())),
                    new SucursalId(UUID.fromString(entity.getSucursalId())),
                    new UsuarioId(UUID.fromString(entity.getUsuarioId())),
                    EstadoTurno.valueOf(entity.getEstado()),
                    Dinero.de(entity.getMontoApertura()),
                    transaccionesDomain,
                    arqueo
            );

        } catch (Exception e) {
            throw new RuntimeException("Error mapeando TurnoCaja desde JPA", e);
        }
    }

    private static TransaccionCaja toTransaccionDomain(TransaccionCajaJpaEntity txEntity) {
        try {
            Constructor<TransaccionCaja> txConstructor = TransaccionCaja.class.getDeclaredConstructor(
                    UUID.class, TipoTransaccion.class, Dinero.class, String.class, java.time.Instant.class
            );
            txConstructor.setAccessible(true);
            return txConstructor.newInstance(
                    UUID.fromString(txEntity.getId()),
                    TipoTransaccion.valueOf(txEntity.getTipo()),
                    Dinero.de(txEntity.getMonto()),
                    txEntity.getReferencia(),
                    txEntity.getFecha()
            );
        } catch (Exception e) {
            throw new RuntimeException("Error mapeando TransaccionCaja desde JPA", e);
        }
    }
}
