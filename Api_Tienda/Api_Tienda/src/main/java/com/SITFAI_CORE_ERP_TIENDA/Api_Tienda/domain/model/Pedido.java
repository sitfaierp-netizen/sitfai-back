package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.DomainEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoConfirmadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoCreadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.event.PedidoCanceladoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.exception.PedidoInvalidoException;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.PedidoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ProductoId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Aggregate Root: {@code Pedido} de venta en el Bounded Context de Api_Tienda (Puerto 8084).
 * <p>
 * Responsabilidades e Invariantes:
 * <ul>
 *   <li>Aislamiento estricto multi-inquilino a través de {@link EmpresaId} (MT-01).</li>
 *   <li>Gobierna el ciclo de vida de sus {@link ItemPedido} / {@link LineaPedido} (REGLA 3).</li>
 *   <li><b>Invariante de inicio:</b> Se inicia en estado {@link EstadoPedido#CREADO}.</li>
 *   <li><b>Invariante de modificación:</b> Solo pedidos en estado modificable ({@code CREADO} / {@code BORRADOR})
 *       pueden añadir, actualizar o remover ítems.</li>
 *   <li><b>Invariante crítica de confirmación:</b> Un pedido NO puede ser confirmado si no contiene ítems.</li>
 *   <li>Registra y acumula {@link DomainEvent} (ej. {@link PedidoConfirmadoEvent}) para su posterior publicación asíncrona (AUD-03).</li>
 * </ul>
 * <p>
 * Aislamiento total: Java 25 puro, sin frameworks ni dependencias externas (REGLA 1, MCP-01).
 */
public class Pedido {

    private final PedidoId id;
    private final EmpresaId empresaId;
    private final ClienteId clienteId;
    private EstadoPedido estado;
    private final List<LineaPedido> lineas;
    private final List<DomainEvent> domainEvents;
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
        this.lineas = new ArrayList<>(Objects.requireNonNull(lineas, "Pedido: la lista de líneas no puede ser null."));
        this.domainEvents = new ArrayList<>();
        this.creadoEn = Objects.requireNonNull(creadoEn, "Pedido: creadoEn es obligatorio (AUD-01).");
        this.actualizadoEn = Objects.requireNonNull(actualizadoEn, "Pedido: actualizadoEn es obligatorio (AUD-01).");
    }

    /**
     * Factory method de creación para iniciar un nuevo Pedido en estado {@link EstadoPedido#CREADO}.
     */
    public static Pedido iniciar(EmpresaId empresaId, ClienteId clienteId) {
        Instant ahora = Instant.now();
        return new Pedido(
                PedidoId.generar(),
                empresaId,
                clienteId,
                EstadoPedido.CREADO,
                new ArrayList<>(),
                ahora,
                ahora
        );
    }

    /**
     * Factory method compatible (alias de iniciar).
     */
    public static Pedido crear(EmpresaId empresaId, ClienteId clienteId) {
        return iniciar(empresaId, clienteId);
    }

    /**
     * Factory method para reconstrucción desde persistencia / repositorios de infraestructura.
     */
    public static Pedido reconstruir(
            PedidoId id,
            EmpresaId empresaId,
            ClienteId clienteId,
            EstadoPedido estado,
            List<LineaPedido> lineas,
            Instant creadoEn,
            Instant actualizadoEn) {

        return new Pedido(id, empresaId, clienteId, estado, lineas, creadoEn, actualizadoEn);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // LÓGICA DE NEGOCIO Y COMPORTAMIENTO
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Añade un ítem al pedido. Si el producto ya existe con el mismo precio unitario, incrementa la cantidad.
     *
     * @param productoId     Identificador del producto.
     * @param cantidad       Cantidad de unidades solicitadas (debe ser > 0).
     * @param precioUnitario Precio unitario del producto.
     * @throws PedidoInvalidoException si el pedido no está en estado {@link EstadoPedido#CREADO}.
     */
    public void agregarItem(ProductoId productoId, Cantidad cantidad, Dinero precioUnitario) {
        validarEstadoModificable("agregar ítems");

        Objects.requireNonNull(productoId, "agregarItem: productoId es obligatorio.");
        Objects.requireNonNull(cantidad, "agregarItem: cantidad es obligatoria.");
        Objects.requireNonNull(precioUnitario, "agregarItem: precioUnitario es obligatorio.");

        // Buscar si ya existe una línea con el mismo producto y precio
        Optional<LineaPedido> existente = this.lineas.stream()
                .filter(l -> l.getProductoId().equals(productoId) && l.getPrecioUnitario().equals(precioUnitario))
                .findFirst();

        if (existente.isPresent()) {
            existente.get().aumentarCantidad(cantidad);
        } else {
            this.lineas.add(LineaPedido.crear(productoId, cantidad, precioUnitario));
        }

        this.actualizadoEn = Instant.now();
    }

    public void agregarItem(ProductoId productoId, int cantidad, Dinero precioUnitario) {
        agregarItem(productoId, Cantidad.de(cantidad), precioUnitario);
    }

    public void agregarLinea(ProductoId productoId, int cantidad, Dinero precioUnitario) {
        agregarItem(productoId, Cantidad.de(cantidad), precioUnitario);
    }

    /**
     * Elimina un ítem por su identificador único.
     *
     * @param itemId Identificador del ítem.
     * @throws PedidoInvalidoException si el pedido no está en estado modificable.
     */
    public void removerItem(UUID itemId) {
        validarEstadoModificable("remover ítems");
        Objects.requireNonNull(itemId, "removerItem: itemId es obligatorio.");

        boolean removido = this.lineas.removeIf(l -> l.getId().equals(itemId));
        if (removido) {
            this.actualizadoEn = Instant.now();
        }
    }

    public void removerLinea(UUID lineaId) {
        removerItem(lineaId);
    }

    /**
     * Calcula el monto total del pedido sumando el subtotal de todas sus líneas.
     */
    public Dinero calcularTotal() {
        if (this.lineas.isEmpty()) {
            return Dinero.cero();
        }

        String moneda = this.lineas.get(0).getPrecioUnitario().moneda();
        Dinero total = Dinero.cero(moneda);

        for (LineaPedido linea : this.lineas) {
            total = total.sumar(linea.subtotal());
        }

        return total;
    }

    /**
     * Solicita la reserva temporal de stock a Inventario.
     * Cambia el estado a {@link EstadoPedido#RESERVANDO_STOCK} y emite el evento {@link PedidoCreadoEvent}.
     *
     * @throws PedidoInvalidoException si la lista de líneas está vacía o si no está en estado modificable.
     */
    public void solicitarReserva() {
        validarEstadoModificable("solicitar reserva");

        if (this.lineas.isEmpty()) {
            throw new PedidoInvalidoException("Un pedido no puede solicitar reserva si su lista de líneas está vacía.");
        }

        this.estado = EstadoPedido.RESERVANDO_STOCK;
        this.actualizadoEn = Instant.now();

        // Registrar Domain Event
        Dinero total = calcularTotal();
        this.domainEvents.add(PedidoCreadoEvent.of(
                this.id,
                this.empresaId,
                this.clienteId,
                total,
                this.lineas
        ));
    }

    /**
     * Invariante Crítica: Confirma el pedido si contiene al menos una línea de detalle.
     * Cambia el estado a {@link EstadoPedido#CONFIRMADO} y registra el evento {@link PedidoConfirmadoEvent}.
     *
     * @throws PedidoInvalidoException si la lista de líneas está vacía o si no está en estado CREADO.
     */
    public void confirmar() {
        validarEstadoModificable("confirmar el pedido");

        if (this.lineas.isEmpty()) {
            throw new PedidoInvalidoException("Un pedido no puede ser confirmado si su lista de líneas está vacía.");
        }

        this.estado = EstadoPedido.CONFIRMADO;
        this.actualizadoEn = Instant.now();

        // Registrar Domain Event
        Dinero total = calcularTotal();
        this.domainEvents.add(PedidoConfirmadoEvent.of(
                this.id,
                this.empresaId,
                this.clienteId,
                total,
                this.lineas
        ));
    }

    /**
     * SAGA Happy Path: Confirma el pedido tras la confirmación exitosa de la reserva de stock en Inventario.
     * <p>
     * Valida fail-fast que el pedido se encuentre en estado {@link EstadoPedido#RESERVANDO_STOCK}.
     * Transiciona el estado a {@link EstadoPedido#CONFIRMADO} y registra el evento {@link PedidoConfirmadoEvent}.
     *
     * @throws PedidoInvalidoException si el pedido no está en estado RESERVANDO_STOCK o si la lista de líneas está vacía.
     */
    public void confirmarReserva() {
        if (this.estado != EstadoPedido.RESERVANDO_STOCK) {
            throw new PedidoInvalidoException(
                    String.format("No se puede confirmar la reserva: el pedido no está en estado RESERVANDO_STOCK. Estado actual: '%s'.", this.estado)
            );
        }

        if (this.lineas.isEmpty()) {
            throw new PedidoInvalidoException("Un pedido no puede ser confirmado si su lista de líneas está vacía.");
        }

        this.estado = EstadoPedido.CONFIRMADO;
        this.actualizadoEn = Instant.now();

        // Registrar Domain Event
        Dinero total = calcularTotal();
        this.domainEvents.add(PedidoConfirmadoEvent.of(
                this.id,
                this.empresaId,
                this.clienteId,
                total,
                this.lineas
        ));
    }

    /**
     * Cancela el pedido.
     *
     * @throws PedidoInvalidoException si el pedido ya está cancelado o confirmado.
     */
    public void cancelar(String motivo) {
        if (this.estado == EstadoPedido.CANCELADO) {
            throw new PedidoInvalidoException("El pedido ya se encuentra cancelado.");
        }

        this.estado = EstadoPedido.CANCELADO;
        this.actualizadoEn = Instant.now();
    }

    /**
     * SAGA Compensation: Cancela el pedido porque el inventario rechazó la reserva de stock (BOD-05).
     * <p>
     * @param motivo El motivo del rechazo enviado por el inventario.
     * @throws PedidoInvalidoException si el pedido no está en estado RESERVANDO_STOCK.
     */
    public void cancelarPorFaltaDeStock(String motivo) {
        if (this.estado != EstadoPedido.RESERVANDO_STOCK) {
            throw new PedidoInvalidoException(
                    String.format("No se puede cancelar por falta de stock: el pedido no está en estado RESERVANDO_STOCK. Estado actual: '%s'.", this.estado)
            );
        }

        this.estado = EstadoPedido.CANCELADO;
        this.actualizadoEn = Instant.now();

        this.domainEvents.add(PedidoCanceladoEvent.of(
                this.id,
                this.empresaId,
                this.clienteId,
                motivo
        ));
    }

    private void validarEstadoModificable(String accion) {
        if (!this.estado.esModificable()) {
            throw new PedidoInvalidoException(
                    String.format("No se puede %s: el pedido está en estado '%s'.", accion, this.estado));
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // MANEJO DE DOMAIN EVENTS (AUD-03)
    // ═════════════════════════════════════════════════════════════════════════

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(this.domainEvents);
    }

    public List<DomainEvent> drainDomainEvents() {
        List<DomainEvent> eventos = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return Collections.unmodifiableList(eventos);
    }

    // ═════════════════════════════════════════════════════════════════════════
    // GETTERS
    // ═════════════════════════════════════════════════════════════════════════

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

    public List<ItemPedido> getItems() {
        return this.lineas.stream().map(LineaPedido::aItemPedido).toList();
    }

    public Instant getCreadoEn() {
        return creadoEn;
    }

    public Instant getActualizadoEn() {
        return actualizadoEn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pedido pedido = (Pedido) o;
        return Objects.equals(id, pedido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Pedido{" +
                "id=" + id +
                ", empresaId=" + empresaId +
                ", clienteId=" + clienteId +
                ", estado=" + estado +
                ", lineas=" + lineas.size() +
                ", total=" + calcularTotal() +
                '}';
    }
}
