package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.event;

public class RolCreadoEvent {
    private final String rolId;
    private final String codigo;
    private final String nombre;

    public RolCreadoEvent(String rolId, String codigo, String nombre) {
        this.rolId = rolId;
        this.codigo = codigo;
        this.nombre = nombre;
    }

    public String getRolId() {
        return rolId;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }
}
