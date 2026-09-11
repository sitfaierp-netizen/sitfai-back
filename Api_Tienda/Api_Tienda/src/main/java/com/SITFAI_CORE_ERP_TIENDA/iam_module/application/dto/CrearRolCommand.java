package com.SITFAI_CORE_ERP_TIENDA.iam_module.application.dto;

import java.util.List;

public class CrearRolCommand {
    private String codigo;
    private String nombre;
    private String descripcion;
    private List<PermisoModuloDto> permisos;

    public CrearRolCommand() {}

    public CrearRolCommand(String codigo, String nombre, String descripcion, List<PermisoModuloDto> permisos) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.permisos = permisos;
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

    public List<PermisoModuloDto> getPermisos() {
        return permisos;
    }

    public void setPermisos(List<PermisoModuloDto> permisos) {
        this.permisos = permisos;
    }
}
