package com.SITFAI_CORE_ERP_TIENDA.iam_module.infrastructure.adapter.out.persistence;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;

@Embeddable
public class RolPermisoEmbeddable {

    @Column(name = "modulo", length = 50, nullable = false)
    private String modulo;

    @Column(name = "accion", length = 50, nullable = false)
    private String accion;

    public RolPermisoEmbeddable() {}

    public RolPermisoEmbeddable(String modulo, String accion) {
        this.modulo = modulo;
        this.accion = accion;
    }

    public String getModulo() {
        return modulo;
    }

    public void setModulo(String modulo) {
        this.modulo = modulo;
    }

    public String getAccion() {
        return accion;
    }

    public void setAccion(String accion) {
        this.accion = accion;
    }
}
