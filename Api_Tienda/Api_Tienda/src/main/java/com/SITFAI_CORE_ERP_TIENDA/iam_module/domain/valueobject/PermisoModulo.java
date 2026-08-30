package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model.ModuloSistema;
import java.util.Set;
import java.util.Collections;

public class PermisoModulo {
    private final ModuloSistema modulo;
    private final Set<String> acciones;

    public PermisoModulo(ModuloSistema modulo, Set<String> acciones) {
        this.modulo = modulo;
        this.acciones = acciones != null ? Set.copyOf(acciones) : Collections.emptySet();
    }

    public ModuloSistema getModulo() {
        return modulo;
    }

    public Set<String> getAcciones() {
        return acciones;
    }
}
