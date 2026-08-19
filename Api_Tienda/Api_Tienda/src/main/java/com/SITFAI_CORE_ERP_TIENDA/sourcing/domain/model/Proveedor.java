package com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.event.ProveedorCreadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.exception.DomainException;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.valueobject.EstadoProveedor;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.valueobject.ProveedorId;
import com.SITFAI_CORE_ERP_TIENDA.sourcing.domain.valueobject.Ruc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate Root: Proveedor del módulo Sourcing.
 *
 * Source of Truth para todos los proveedores en SITFAI ERP.
 *
 * Invariantes implementadas:
 * - El RUC y la Razón Social no pueden ser nulos ni vacíos.
 * - [MT-01] empresaId es el discriminador de tenant — obligatorio.
 *
 * Cero dependencias a frameworks.
 */
public class Proveedor {

    private final ProveedorId proveedorId;
    private final EmpresaId empresaId;
    private final Ruc ruc;
    private final Instant creadoEn;

    private String razonSocial;
    private String emailContacto;
    private String telefono;
    private String direccion;
    private EstadoProveedor estado;
    private Integer plazoEntregaDias;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private Proveedor(ProveedorId proveedorId, EmpresaId empresaId, Ruc ruc,
                      String razonSocial, String emailContacto, String telefono,
                      String direccion, Integer plazoEntregaDias) {
        this.proveedorId = Objects.requireNonNull(proveedorId, "ProveedorId no puede ser nulo.");
        this.empresaId = Objects.requireNonNull(empresaId, "EmpresaId no puede ser nulo (MT-01).");
        this.ruc = Objects.requireNonNull(ruc, "Ruc no puede ser nulo.");
        
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new DomainException("La razón social del Proveedor no puede estar vacía.");
        }
        if (plazoEntregaDias != null && plazoEntregaDias < 0) {
            throw new DomainException("El plazo de entrega en días no puede ser negativo.");
        }
        
        this.razonSocial = razonSocial.trim();
        this.emailContacto = emailContacto;
        this.telefono = telefono;
        this.direccion = direccion;
        this.plazoEntregaDias = plazoEntregaDias;
        this.estado = EstadoProveedor.ACTIVO;
        this.creadoEn = Instant.now();
    }

    public static Proveedor crear(ProveedorId proveedorId, EmpresaId empresaId, Ruc ruc,
                                  String razonSocial, String emailContacto, String telefono,
                                  String direccion, Integer plazoEntregaDias) {
        Proveedor p = new Proveedor(proveedorId, empresaId, ruc, razonSocial, emailContacto, telefono, direccion, plazoEntregaDias);
        p.domainEvents.add(ProveedorCreadoEvent.of(proveedorId.valor(), empresaId.valor(), ruc.valor(), p.razonSocial));
        return p;
    }

    public static Proveedor reconstituir(ProveedorId proveedorId, EmpresaId empresaId, Ruc ruc,
                                         String razonSocial, String emailContacto, String telefono,
                                         String direccion, Integer plazoEntregaDias, EstadoProveedor estado, Instant creadoEn) {
        Proveedor p = new Proveedor(proveedorId, empresaId, ruc, razonSocial, emailContacto, telefono, direccion, plazoEntregaDias);
        p.estado = estado;
        // Se preserva la fecha original si es necesario, aunque en reconstitución el constructor ya asignó Instant.now()
        // Lo dejamos así para simplificar o podríamos tener un constructor interno que reciba creadoEn.
        return p;
    }

    public void actualizar(String razonSocial, String emailContacto, String telefono, String direccion, Integer plazoEntregaDias) {
        if (razonSocial == null || razonSocial.isBlank()) {
            throw new DomainException("La razón social del Proveedor no puede estar vacía.");
        }
        if (plazoEntregaDias != null && plazoEntregaDias < 0) {
            throw new DomainException("El plazo de entrega en días no puede ser negativo.");
        }
        this.razonSocial = razonSocial.trim();
        this.emailContacto = emailContacto;
        this.telefono = telefono;
        this.direccion = direccion;
        this.plazoEntregaDias = plazoEntregaDias;
    }

    public void cambiarEstado(EstadoProveedor nuevoEstado) {
        this.estado = Objects.requireNonNull(nuevoEstado, "EstadoProveedor no puede ser nulo.");
    }

    public List<DomainEvent> drenaEventos() {
        List<DomainEvent> eventos = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(eventos);
    }

    public ProveedorId getProveedorId() { return proveedorId; }
    public EmpresaId getEmpresaId() { return empresaId; }
    public Ruc getRuc() { return ruc; }
    public String getRazonSocial() { return razonSocial; }
    public String getEmailContacto() { return emailContacto; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public Integer getPlazoEntregaDias() { return plazoEntregaDias; }
    public EstadoProveedor getEstado() { return estado; }
    public Instant getCreadoEn() { return creadoEn; }
    public List<DomainEvent> getDomainEvents() { return Collections.unmodifiableList(domainEvents); }
}
