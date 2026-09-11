package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.ModuloSistema;
import java.util.Set;

public class PermisoModuloDto {
    private ModuloSistema modulo;
    private Set<String> acciones;

    public PermisoModuloDto() {}

    public PermisoModuloDto(ModuloSistema modulo, Set<String> acciones) {
        this.modulo = modulo;
        this.acciones = acciones;
    }

    public ModuloSistema getModulo() {
        return modulo;
    }

    public void setModulo(ModuloSistema modulo) {
        this.modulo = modulo;
    }

    public Set<String> getAcciones() {
        return acciones;
    }

    public void setAcciones(Set<String> acciones) {
        this.acciones = acciones;
    }
}
