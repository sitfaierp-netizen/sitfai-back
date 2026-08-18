package com.SITFAI_CORE_ERP_TIENDA.pos.infrastructure.adapter.out.persistence;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.EstadoTurno;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TipoTransaccion;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TransaccionCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.TurnoCaja;
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

        List<TransaccionCajaJpaEntity> lineasEntity = domain.getTransacciones().stream()
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

        entity.getTransacciones().addAll(lineasEntity);
        return entity;
    }

    public static TurnoCaja toDomainEntity(TurnoCajaJpaEntity entity) {
        try {
            // Reconstruir TurnoCaja saltando los invariantes de apertura usando reflection para el constructor privado
            Constructor<TurnoCaja> constructor = TurnoCaja.class.getDeclaredConstructor(
                    TurnoId.class, EmpresaId.class, CajaId.class, SucursalId.class, UsuarioId.class, Dinero.class
            );
            constructor.setAccessible(true);
            
            TurnoCaja turno = constructor.newInstance(
                    new TurnoId(UUID.fromString(entity.getId())),
                    new EmpresaId(UUID.fromString(entity.getEmpresaId())),
                    new CajaId(UUID.fromString(entity.getCajaId())),
                    new SucursalId(UUID.fromString(entity.getSucursalId())),
                    new UsuarioId(UUID.fromString(entity.getUsuarioId())),
                    Dinero.de(entity.getMontoApertura())
            );

            Field estadoField = TurnoCaja.class.getDeclaredField("estado");
            estadoField.setAccessible(true);
            estadoField.set(turno, EstadoTurno.valueOf(entity.getEstado()));

            Field transaccionesField = TurnoCaja.class.getDeclaredField("transacciones");
            transaccionesField.setAccessible(true);
            List<TransaccionCaja> txs = (List<TransaccionCaja>) transaccionesField.get(turno);

            for (TransaccionCajaJpaEntity txEntity : entity.getTransacciones()) {
                Constructor<TransaccionCaja> txConstructor = TransaccionCaja.class.getDeclaredConstructor(
                        UUID.class, TipoTransaccion.class, Dinero.class, String.class, java.time.Instant.class
                );
                txConstructor.setAccessible(true);
                TransaccionCaja tx = txConstructor.newInstance(
                        UUID.fromString(txEntity.getId()),
                        TipoTransaccion.valueOf(txEntity.getTipo()),
                        Dinero.de(txEntity.getMonto()),
                        txEntity.getReferencia(),
                        txEntity.getFecha()
                );
                txs.add(tx);
            }

            return turno;

        } catch (Exception e) {
            throw new RuntimeException("Error mapeando TurnoCaja desde JPA", e);
        }
    }
}
