package com.SITFAI_CORE_ERP_TIENDA.shared.domain.exception;

/**
 * Excepción de Dominio lanzada cuando se intenta registrar o actualizar una entidad 
 * violando una restricción de unicidad de negocio (ej. código duplicado por empresa).
 */
public class RegistroDuplicadoException extends RuntimeException {

    private final String entidad;
    private final String campo;
    private final String valor;

    public RegistroDuplicadoException(String entidad, String campo, String valor) {
        super(String.format("El registro de %s ya existe para el campo %s con valor '%s'", entidad, campo, valor));
        this.entidad = entidad;
        this.campo = campo;
        this.valor = valor;
    }

    public String getEntidad() {
        return entidad;
    }

    public String getCampo() {
        return campo;
    }

    public String getValor() {
        return valor;
    }
}
