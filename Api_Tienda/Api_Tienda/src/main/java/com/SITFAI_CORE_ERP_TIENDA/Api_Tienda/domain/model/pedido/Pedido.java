package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.event.PedidoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.EstadoPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.pedido.vo.ProductoId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Aggregate Root: {@code Pedido} de venta.
 * <p>
 * Responsabilidades e invariantes:
 * <ul>
 *   <li><b>Aislamiento Multi-Tenant (MT-01):</b> Encapsula {@link EmpresaId} como clave de partición raíz.</li>
 *   <li><b>Identidad de Dominio (REGLA-3):</b> Encapsula {@link PedidoId} como Value Object.</li>
 *   <li><b>Invariante de inicio:</b> Se instancia en estado {@link EstadoPedido#PENDIENTE} o {@link EstadoPedido#CREADO}.</li>
 *   <li><b>Modificación controlada:</b> Solo en estado modificable se pueden añadir líneas de detalle.</li>
 *   <li><b>Invariante de confirmación:</b> No se puede confirmar un pedido sin al menos una línea de producto.</li>
 *   <li><b>Transición transaccional:</b> {@link #confirmar()} transiciona a {@code CONFIRMADO} y registra el evento {@link PedidoConfirmadoEvent}.</li>
 * </ul>
 * <p>
 * Cero dependencias a frameworks, JPA o Spring (REGLA-1, Clean Architecture).
 */
public class Pedido {

    private final PedidoId id;
    private final EmpresaId empresaId;
    private final ClienteId clienteId;
    private EstadoPedido estado;
    private final List<LineaPedido> lineas;
    private final List<PedidoConfirmadoEvent> domainEvents;
    private final Instant creadoEn;
    private Instant actualizadoEn;

    private Pedido(
            PedidoId id,
            EmpresaId empresaId,
            ClienteId clienteId,
            EstadoPedido estado,
            List<LineaPedido> lineas,
            Instant creadoEn,
            Instant actualizadoEn) {

        this.id = Objects.requireNonNull(id, "Pedido: id es obligatorio.");
        this.empresaId = Objects.requireNonNull(empresaId, "Pedido: empresaId es obligatorio (MT-01).");
        this.clienteId = Objects.requireNonNull(clienteId, "Pedido: clienteId es obligatorio.");
        this.estado = Objects.requireNonNull(estado, "Pedido: estado es obligatorio.");
        this.lineas = new ArrayList<>(Objects.requireNonNullElseGet(lineas, ArrayList::new));
        this.domainEvents = new ArrayList<>();
        this.creadoEn = creadoEn != null ? creadoEn : Instant.now();
        this.actualizadoEn = actualizadoEn != null ? actualizadoEn : this.creadoEn;
    }

    // =========================================================================
    // FACTORY METHODS
    // =========================================================================

    /**
     * Factory Method: Crea un nuevo Pedido en estado {@link EstadoPedido#PENDIENTE}.
     *
     * @param empresaId Tenant autenticado (MT-01).
     * @param clienteId Cliente que realiza el pedido.
     * @return Nueva instancia del agregado en estado PENDIENTE.
     */
    public static Pedido crear(EmpresaId empresaId, ClienteId clienteId) {
        return crear(empresaId, PedidoId.generar(), clienteId);
    }

    /**
     * Factory Method con ID explícito.
     */
    public static Pedido crear(EmpresaId empresaId, PedidoId id, ClienteId clienteId) {
        return new Pedido(
                id,
                empresaId,
                clienteId,
                EstadoPedido.PENDIENTE,
                new ArrayList<>(),
                Instant.now(),
                Instant.now()
        );
    }

    /**
     * Alias compatible de inicio.
     */
    public static Pedido iniciar(EmpresaId empresaId, ClienteId clienteId) {
        return crear(empresaId, clienteId);
    }

    public static Pedido iniciar(
            com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId empresaId,
            com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId clienteId) {
        return crear(
                new EmpresaId(empresaId.valor()),
                new ClienteId(clienteId.valor())
        );
    }

    public static Pedido iniciar(EmpresaId empresaId, PedidoId id, ClienteId clienteId) {
        return crear(empresaId, id, clienteId);
    }

    /**
     * Factory Method para reconstitución desde capa de persistencia (sin generar eventos).
     */
    public static Pedido reconstituir(
            PedidoId id,
            EmpresaId empresaId,
            ClienteId clienteId,
            EstadoPedido estado,
            List<LineaPedido> lineas,
            Instant creadoEn,
            Instant actualizadoEn) {
        return new Pedido(id, empresaId, clienteId, estado, lineas, creadoEn, actualizadoEn);
    }

    public static Pedido reconstruir(
            PedidoId id,
            EmpresaId empresaId,
            ClienteId clienteId,
            EstadoPedido estado,
            List<LineaPedido> lineas,
            Instant creadoEn,
            Instant actualizadoEn) {
        return reconstituir(id, empresaId, clienteId, estado, lineas, creadoEn, actualizadoEn);
    }

    // =========================================================================
    // COMPORTAMIENTO DE DOMINIO Y REGLAS DE NEGOCIO
    // =========================================================================

    public void agregarLinea(ProductoId productoId, int cantidad, Dinero precioUnitario) {
        agregarLinea(new LineaPedido(productoId, cantidad, precioUnitario));
    }

    public void agregarLinea(LineaPedido linea) {
        Objects.requireNonNull(linea, "Pedido: la línea a agregar no puede ser nula.");
        validarEstadoModificable("agregar líneas");
        this.lineas.add(linea);
        this.actualizadoEn = Instant.now();
    }

    public void agregarItem(ProductoId productoId, int cantidad, Dinero precioUnitario) {
        agregarLinea(productoId, cantidad, precioUnitario);
    }

    public void agregarItem(
            com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ProductoId productoId,
            int cantidad,
            com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero precioUnitario) {
        agregarLinea(
                new ProductoId(productoId.valor()),
                cantidad,
                Dinero.de(precioUnitario.monto())
        );
    }

    public void removerItem(UUID lineaId) {
        validarEstadoModificable("remover líneas");
        this.lineas.removeIf(l -> Objects.equals(l.getId(), lineaId));
        this.actualizadoEn = Instant.now();
    }

    public Dinero calcularTotal() {
        Dinero total = Dinero.cero();
        for (LineaPedido linea : lineas) {
            total = total.sumar(linea.calcularSubtotal());
        }
        return total;
    }

    public void solicitarReserva() {
        this.estado = EstadoPedido.RESERVANDO_STOCK;
        this.actualizadoEn = Instant.now();
    }

    public void confirmarReserva() {
        confirmar();
    }

    public void confirmar() {
        if (this.estado == EstadoPedido.CONFIRMADO) {
            return; // Idempotente
        }
        if (this.estado == EstadoPedido.CANCELADO) {
            throw new IllegalStateException("No se puede confirmar un pedido en estado CANCELADO.");
        }
        if (this.lineas.isEmpty()) {
            throw new IllegalStateException("No se puede confirmar un pedido sin líneas de productos.");
        }

        this.estado = EstadoPedido.CONFIRMADO;
        this.actualizadoEn = Instant.now();

        // Acumular evento de dominio
        this.domainEvents.add(PedidoConfirmadoEvent.of(
                this.empresaId,
                this.id,
                this.clienteId,
                calcularTotal(),
                this.lineas
        ));
    }

    public void cancelar() {
        if (this.estado == EstadoPedido.CANCELADO) {
            return; // Idempotente
        }
        this.estado = EstadoPedido.CANCELADO;
        this.actualizadoEn = Instant.now();
    }

    public void cancelar(String motivo) {
        cancelar();
    }

    public void cancelarPorFaltaDeStock(String motivo) {
        cancelar();
    }

    private void validarEstadoModificable(String accion) {
        if (!this.estado.esModificable()) {
            throw new IllegalStateException(
                    "Operación inválida: no se puede " + accion + " en un pedido con estado " + this.estado + ".");
        }
    }

    // =========================================================================
    // GESTIÓN DE EVENTOS DE DOMINIO (REGLA-3)
    // =========================================================================

    public List<PedidoConfirmadoEvent> pullDomainEvents() {
        List<PedidoConfirmadoEvent> eventos = Collections.unmodifiableList(new ArrayList<>(domainEvents));
        domainEvents.clear();
        return eventos;
    }

    public List<PedidoConfirmadoEvent> drainDomainEvents() {
        return pullDomainEvents();
    }

    public List<PedidoConfirmadoEvent> peekDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    // =========================================================================
    // GETTERS (SOLO LECTURA)
    // =========================================================================

    public PedidoId getId() {
        return id;
    }

    public EmpresaId getEmpresaId() {
        return empresaId;
    }

    public ClienteId getClienteId() {
        return clienteId;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public List<LineaPedido> getLineas() {
        return Collections.unmodifiableList(lineas);
    }

    public List<LineaPedido> getItems() {
        return Collections.unmodifiableList(lineas);
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }

    @Override
    public String toString() {
        return "Pedido{id=" + id + ", empresaId=" + empresaId + ", clienteId=" + clienteId +
                ", estado=" + estado + ", total=" + calcularTotal() + ", totalLineas=" + lineas.size() + "}";
    }
}
