package com.SITFAI_CORE_ERP_TIENDA.pos.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.TurnoCaja;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.CajaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.pos.domain.model.turno.vo.TurnoId;

import java.util.List;
import java.util.Optional;

/**
 * Output Port: Repositorio de Dominio para el Agregado TurnoCaja.
 * <p>
 * Regla MT-01: Exige de forma estricta e inquebrantable el parámetro {@link EmpresaId}
 * en cada firma de método para garantizar la barrera de partición multitenant.
 * Prohibido introducir anotaciones o dependencias de Spring o JPA en esta interfaz.
 */
public interface TurnoCajaRepository {

    /**
     * Guarda o actualiza el estado del turno de caja en el contexto de la empresa.
     *
     * @param turno Agregado TurnoCaja a persistir.
     * @param empresaId Identificador de partición del tenant (MT-01).
     * @return Agregado TurnoCaja persistido.
     */
    TurnoCaja guardar(TurnoCaja turno, EmpresaId empresaId);

    /**
     * Busca un turno de caja por su identificador único dentro del tenant.
     *
     * @param id Identificador único del turno.
     * @param empresaId Identificador de partición del tenant (MT-01).
     * @return Optional con el turno encontrado o vacío.
     */
    Optional<TurnoCaja> buscarPorId(TurnoId id, EmpresaId empresaId);

    /**
     * Busca el turno actualmente en estado ABIERTO para una caja específica dentro del tenant.
     *
     * @param cajaId Identificador de la caja registradora.
     * @param empresaId Identificador de partición del tenant (MT-01).
     * @return Optional con el turno abierto o vacío si la caja está cerrada.
     */
    Optional<TurnoCaja> buscarTurnoAbiertoPorCaja(CajaId cajaId, EmpresaId empresaId);

    /**
     * Lista todos los turnos pertenecientes a la empresa especificada.
     *
     * @param empresaId Identificador de partición del tenant (MT-01).
     * @return Lista inmutable de turnos de la empresa.
     */
    List<TurnoCaja> listarPorEmpresa(EmpresaId empresaId);
}
