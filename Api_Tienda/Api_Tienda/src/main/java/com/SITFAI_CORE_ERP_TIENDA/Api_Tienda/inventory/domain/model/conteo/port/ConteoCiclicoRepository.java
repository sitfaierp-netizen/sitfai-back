package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.port;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.ConteoCiclico;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.ConteoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.conteo.vo.EstadoConteo;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de Salida (Output Port): Contrato de persistencia pura para el Agregado {@link ConteoCiclico}.
 * <p>
 * Regla MT-01: Exige {@link EmpresaId} en todas sus firmas para garantizar el aislamiento multi-tenant.
 * Regla REGLA-1: Interfaz pura de Java, sin dependencias de frameworks ni JPA.
 */
public interface ConteoCiclicoRepository {

    /**
     * Persiste o actualiza un Conteo Cíclico junto con todos sus detalles.
     *
     * @param conteo Agregado raíz a persistir.
     */
    void guardar(ConteoCiclico conteo);

    /**
     * Recupera un Conteo Cíclico por su identificador único dentro del tenant.
     *
     * @param empresaId Discriminador de tenant (MT-01).
     * @param id Identificador único del conteo.
     * @return Conteo si existe y pertenece al tenant.
     */
    Optional<ConteoCiclico> buscarPorId(EmpresaId empresaId, ConteoId id);

    /**
     * Lista todos los conteos cíclicos programados o ejecutados en una bodega del tenant.
     *
     * @param empresaId Discriminador de tenant (MT-01).
     * @param bodegaId Bodega auditada.
     * @return Lista de conteos cíclicos asociados.
     */
    List<ConteoCiclico> listarPorBodega(EmpresaId empresaId, BodegaId bodegaId);

    /**
     * Lista los conteos cíclicos de un tenant filtrados por su estado operativo.
     *
     * @param empresaId Discriminador de tenant (MT-01).
     * @param estado Estado del ciclo de vida.
     * @return Lista de conteos en el estado indicado.
     */
    List<ConteoCiclico> listarPorEstado(EmpresaId empresaId, EstadoConteo estado);

    /**
     * Verifica la existencia de un conteo cíclico para un tenant.
     *
     * @param empresaId Discriminador de tenant (MT-01).
     * @param id Identificador único del conteo.
     * @return Verdadero si existe en el tenant especificado.
     */
    boolean existePorId(EmpresaId empresaId, ConteoId id);
}
