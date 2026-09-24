package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence.mapper;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class TurnoPersistenceMapper {

    private TurnoPersistenceMapper() {
    }

    public static TurnoCajaJpaEntity toJpaEntity(TurnoCaja domain) {
        Objects.requireNonNull(domain, "TurnoPersistenceMapper: domain no puede ser null.");

        TurnoCajaJpaEntity entity = new TurnoCajaJpaEntity();
        entity.setId(domain.getId().value().toString());
        entity.setEmpresaId(domain.getEmpresaId().value().toString());
        entity.setCajaId(domain.getCajaId().value().toString());
        entity.setSucursalId(domain.getSucursalId().value().toString());
        entity.setUsuarioId(domain.getUsuarioId().value().toString());
        entity.setEstado(domain.getEstado().name());
        entity.setMontoApertura(domain.getMontoApertura().valor());
        entity.setVersion(domain.getVersion());

        if (domain.getArqueo() != null) {
            entity.setMontoEsperado(domain.getArqueo().balanceEsperado().valor());
            entity.setMontoDeclarado(domain.getArqueo().montoDeclarado().valor());
            entity.setDiferencia(domain.getArqueo().diferencia().valor());
        } else {
            entity.setMontoEsperado(null);
            entity.setMontoDeclarado(null);
            entity.setDiferencia(null);
        }

        if (domain.getTransacciones() != null) {
            List<TransaccionCajaJpaEntity> txs = new ArrayList<>();
            for (TransaccionCaja tx : domain.getTransacciones()) {
                TransaccionCajaJpaEntity txEntity = new TransaccionCajaJpaEntity();
                txEntity.setId(tx.getId().toString());
                txEntity.setTurno(entity);
                txEntity.setEmpresaId(domain.getEmpresaId().value().toString());
                txEntity.setTipo(tx.getTipo().name());
                txEntity.setMonto(tx.getMonto().valor());
                txEntity.setReferencia(tx.getReferencia());
                txEntity.setFecha(tx.getFecha());
                txs.add(txEntity);
            }
            entity.setTransacciones(txs);
        }

        return entity;
    }

    public static TurnoCaja toDomain(TurnoCajaJpaEntity entity) {
        Objects.requireNonNull(entity, "TurnoPersistenceMapper: entity no puede ser null.");

        TurnoId turnoId = new TurnoId(UUID.fromString(entity.getId()));
        EmpresaId empresaId = new EmpresaId(UUID.fromString(entity.getEmpresaId()));
        CajaId cajaId = new CajaId(UUID.fromString(entity.getCajaId()));
        SucursalId sucursalId = new SucursalId(UUID.fromString(entity.getSucursalId()));
        UsuarioId usuarioId = new UsuarioId(UUID.fromString(entity.getUsuarioId()));
        EstadoTurno estado = EstadoTurno.valueOf(entity.getEstado());
        Dinero montoApertura = Dinero.de(entity.getMontoApertura());

        List<TransaccionCaja> transacciones = new ArrayList<>();
        if (entity.getTransacciones() != null) {
            for (TransaccionCajaJpaEntity txEntity : entity.getTransacciones()) {
                TransaccionCaja txDomain = TransaccionCaja.reconstituir(
                        UUID.fromString(txEntity.getId()),
                        TipoTransaccion.valueOf(txEntity.getTipo()),
                        Dinero.de(txEntity.getMonto()),
                        txEntity.getReferencia(),
                        txEntity.getFecha()
                );
                transacciones.add(txDomain);
            }
        }

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
                turnoId,
                empresaId,
                cajaId,
                sucursalId,
                usuarioId,
                estado,
                montoApertura,
                transacciones,
                arqueo,
                entity.getVersion()
        );
    }
}
