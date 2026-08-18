package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.mapper;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.EstadoPedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.model.Pedido;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ClienteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.Dinero;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity.LineaPedidoJpaEntity;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.infrastructure.adapter.out.persistence.entity.PedidoJpaEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Infraestructura: PedidoPersistenceMapper")
class PedidoPersistenceMapperTest {

    private final PedidoPersistenceMapper mapper = new PedidoPersistenceMapper();

    @Test
    @DisplayName("Debe convertir Pedido (Dominio) a PedidoJpaEntity bidireccionalmente")
    void debeMapearDominioAEntityYViceversa() {
        EmpresaId empresaId = EmpresaId.generar();
        ClienteId clienteId = ClienteId.generar();
        ProductoId productoId = ProductoId.generar();

        Pedido pedido = Pedido.crear(empresaId, clienteId);
        pedido.agregarLinea(productoId, 3, Dinero.de(new BigDecimal("15.50"), "USD"));

        // Dominio -> Entity
        PedidoJpaEntity entity = mapper.toEntity(pedido);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(pedido.getId().valor());
        assertThat(entity.getEmpresaId()).isEqualTo(empresaId.valor());
        assertThat(entity.getClienteId()).isEqualTo(clienteId.valor());
        assertThat(entity.getEstado()).isEqualTo("CREADO");
        assertThat(entity.getTotal()).isEqualByComparingTo(new BigDecimal("46.50"));
        assertThat(entity.getMoneda()).isEqualTo("USD");
        assertThat(entity.getLineas()).hasSize(1);

        LineaPedidoJpaEntity lineaEntity = entity.getLineas().get(0);
        assertThat(lineaEntity.getProductoId()).isEqualTo(productoId.valor());
        assertThat(lineaEntity.getCantidad()).isEqualTo(3);
        assertThat(lineaEntity.getPrecioUnitario()).isEqualByComparingTo(new BigDecimal("15.50"));
        assertThat(lineaEntity.getSubtotal()).isEqualByComparingTo(new BigDecimal("46.50"));

        // Entity -> Dominio
        Pedido domainReconstituido = mapper.toDomain(entity);

        assertThat(domainReconstituido).isNotNull();
        assertThat(domainReconstituido.getId()).isEqualTo(pedido.getId());
        assertThat(domainReconstituido.getEmpresaId()).isEqualTo(empresaId);
        assertThat(domainReconstituido.getClienteId()).isEqualTo(clienteId);
        assertThat(domainReconstituido.getEstado()).isEqualTo(EstadoPedido.CREADO);
        assertThat(domainReconstituido.getLineas()).hasSize(1);
        assertThat(domainReconstituido.calcularTotal().monto()).isEqualByComparingTo(new BigDecimal("46.50"));
    }
}
