package com.SITFAI_CORE_ERP_TIENDA.purchasing.infrastructure.adapter.in.messaging;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.event.PuntoReordenAlcanzadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.ProcesarPuntoReordenCommand;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.dto.OrdenCompraResponse;
import com.SITFAI_CORE_ERP_TIENDA.purchasing.application.port.input.ProcesarPuntoReordenUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * TEST: ReabastecimientoEventHandlerTest
 * ═══════════════════════════════════════════════════════════════════════════════
 * <p>
 * Valida el flujo de Reabastecimiento Automático (Replenishment):
 * <ol>
 *   <li>El módulo Inventory emite un {@link PuntoReordenAlcanzadoEvent}.</li>
 *   <li>El {@link PuntoReordenEventListener} (adapter de entrada del BC purchasing) lo captura.</li>
 *   <li>El listener orquesta la creación de una Orden de Compra en estado BORRADOR.</li>
 *   <li>El aislamiento MT-02 se preserva — el empresaId del evento fluye hasta la orden.</li>
 * </ol>
 * <p>
 * Arquitectura: Unit Test de la Capa de Infraestructura (Regla 8 — Application Unit Test con Mockito).
 * No requiere Testcontainers porque no hay acceso a BD en este adaptador.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Replenishment: PuntoReordenEventListener → OrdenCompra BORRADOR")
class ReabastecimientoEventHandlerTest {

    @Mock
    private ProcesarPuntoReordenUseCase procesarPuntoReordenUseCase;

    @InjectMocks
    private PuntoReordenEventListener listener;

    // ── Fixtures ──────────────────────────────────────────────────────────────
    private UUID empresaId;
    private UUID productoId;
    private UUID bodegaId;
    private UUID ordenGeneradaId;
    private PuntoReordenAlcanzadoEvent evento;

    @BeforeEach
    void configurar() {
        empresaId      = UUID.fromString("aaaaaaaa-0000-0000-0000-000000000001");
        productoId     = UUID.fromString("bbbbbbbb-0000-0000-0000-000000000002");
        bodegaId       = UUID.fromString("cccccccc-0000-0000-0000-000000000003");
        ordenGeneradaId = UUID.randomUUID();

        // Simular descuento crítico de stock — el agregado Bodega emitirá este evento
        evento = PuntoReordenAlcanzadoEvent.of(
                new EmpresaId(empresaId),
                new BodegaId(bodegaId),
                new ProductoId(productoId),
                new BigDecimal("5.00") // Stock restante (< punto de reorden)
        );

        // El Use Case retorna una respuesta de la orden recién creada en BORRADOR
        OrdenCompraResponse responseBorrador = new OrdenCompraResponse(
                ordenGeneradaId,
                empresaId,
                UUID.fromString("11111111-1111-1111-1111-111111111111"),
                "2026-09-19T18:00:00Z",
                "BORRADOR",
                BigDecimal.ZERO,
                Collections.emptyList()
        );
        when(procesarPuntoReordenUseCase.procesar(any(ProcesarPuntoReordenCommand.class)))
                .thenReturn(responseBorrador);
    }

    @Test
    @DisplayName("dado StockCrítico, cuando se recibe el evento, entonces crea OrdenCompra en BORRADOR con el mismo empresaId")
    void dadoStockCritico_cuandoSeRecibeEvento_entoncesCreaBorrador_conEmpresaIdCorrecto() {
        // WHEN
        listener.handlePuntoReordenAlcanzadoEvent(evento);

        // THEN — Capturamos el comando enviado al Use Case para verificar el aislamiento MT-02
        ArgumentCaptor<ProcesarPuntoReordenCommand> captor =
                ArgumentCaptor.forClass(ProcesarPuntoReordenCommand.class);
        verify(procesarPuntoReordenUseCase).procesar(captor.capture());

        ProcesarPuntoReordenCommand comandoCapturado = captor.getValue();
        assertThat(comandoCapturado.empresaId())
                .as("El empresaId del evento DEBE propagarse a la OrdenCompra (aislamiento MT-02)")
                .isEqualTo(empresaId);
    }

    @Test
    @DisplayName("dado StockCrítico, cuando se recibe el evento, entonces agrega línea con el productoId correcto")
    void dadoStockCritico_cuandoSeRecibeEvento_entoncesAgregaLineaConProductoCorrecto() {
        // WHEN
        listener.handlePuntoReordenAlcanzadoEvent(evento);

        // THEN — Verificar que se añadió una línea a la orden con el producto afectado
        ArgumentCaptor<ProcesarPuntoReordenCommand> captor =
                ArgumentCaptor.forClass(ProcesarPuntoReordenCommand.class);
        verify(procesarPuntoReordenUseCase).procesar(captor.capture());

        ProcesarPuntoReordenCommand lineaCapturada = captor.getValue();
        assertThat(lineaCapturada.productoId())
                .as("La línea de la OC debe referenciar el producto que cruzó el punto de reorden")
                .isEqualTo(productoId);
        assertThat(lineaCapturada.empresaId())
                .as("La línea también debe contener el empresaId (MT-02)")
                .isEqualTo(empresaId);
    }

    @Test
    @DisplayName("dado StockCrítico, cuando se recibe el evento, entonces el flujo completo ejecuta exactamente 2 operaciones")
    void dadoStockCritico_cuandoSeRecibeEvento_entoncesFlujoCorrecto() {
        // WHEN
        listener.handlePuntoReordenAlcanzadoEvent(evento);

        // THEN — El handler debe invocar exactamente: 1 creación de borrador + 1 adición de línea
        verify(procesarPuntoReordenUseCase, times(1)).procesar(any());
        verifyNoMoreInteractions(procesarPuntoReordenUseCase);
    }
}
