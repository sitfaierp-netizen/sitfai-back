package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.Recepcion;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.recepcion.vo.RecepcionId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;

import java.util.Optional;

/**
 * Output Port: Repositorio de Recepción (Inbound Logistics).
 * <p>
 * Interfaz pura de Java (Arquitectura Hexagonal). Libre de dependencias
 * de frameworks (JPA, Spring Data, etc).
 */
public interface RecepcionRepository {

    /**
     * Guarda o actualiza un Agregado Recepcion.
     * 
     * @param recepcion El agregado a guardar
     */
    void guardar(Recepcion recepcion);

    /**
     * Busca una recepción por su ID, validando siempre el tenant (MT-01).
     *
     * @param id El identificador único de la Recepción
     * @param empresaId El identificador del tenant (aislamiento)
     * @return Opcional con la recepción si existe y pertenece al tenant
     */
    Optional<Recepcion> buscarPorIdYEmpresaId(RecepcionId id, EmpresaId empresaId);
}
