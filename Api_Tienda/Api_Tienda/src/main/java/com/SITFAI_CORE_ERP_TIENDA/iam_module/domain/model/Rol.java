package com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.iam_module.domain.valueobject.PermisoModulo;
import java.util.List;
import java.util.ArrayList;

public class Rol {
    private String id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private List<PermisoModulo> permisos;

    public Rol(String id, String codigo, String nombre, String descripcion, List<PermisoModulo> permisos) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.permisos = permisos != null ? new ArrayList<>(permisos) : new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<PermisoModulo> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<PermisoModulo> permisos) {
        this.permisos = permisos;
    }
}
