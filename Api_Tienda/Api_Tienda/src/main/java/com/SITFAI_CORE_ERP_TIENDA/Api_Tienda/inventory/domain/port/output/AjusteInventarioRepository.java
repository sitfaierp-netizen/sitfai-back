package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.port.output;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.AjusteInventario;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.ajuste.vo.AjusteInventarioId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;

import java.util.Optional;

/**
 * Puerto de Salida (Driven Port): Repositorio del Agregado AjusteInventario.
 */
public interface AjusteInventarioRepository {
    
    void guardar(AjusteInventario ajuste);
    
    Optional<AjusteInventario> buscarPorIdYEmpresaId(AjusteInventarioId id, EmpresaId empresaId);
}
