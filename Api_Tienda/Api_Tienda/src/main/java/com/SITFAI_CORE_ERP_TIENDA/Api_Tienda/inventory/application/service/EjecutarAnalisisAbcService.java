package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.AnalisisAbcResponse;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.EjecutarAnalisisAbcCommand;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.dto.ProductoMetricaSalidaDto;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.input.EjecutarAnalisisAbcUseCase;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.TenantProviderPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.application.port.output.query.MetricaMovimientoQueryPort;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.ClasificacionProducto;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.event.ProductoReclasificadoEvent;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.port.ClasificacionProductoRepository;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.BodegaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.CategoriaABC;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.MetricaMovimiento;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.clasificacion.vo.ProductoId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Servicio de Aplicación: Orquestador transaccional para la ejecución masiva del Análisis ABC de Inventario.
 * <p>
 * Regla MT-01: Extrae de forma segura el tenant autenticado mediante {@link TenantProviderPort}.
 * Regla 1 (Clean Architecture): Orquesta el dominio puro y puertos de salida sin acoplarse a frameworks en dominio.
 * Aplica la Ley de Pareto:
 * - Categoría A: Top 80% del valor total despachado (o frecuencia si no hay valor).
 * - Categoría B: Siguiente 15% (80% a 95%).
 * - Categoría C: Último 5% (95% a 100%) o ítems sin rotación.
 */
@Service
public class EjecutarAnalisisAbcService implements EjecutarAnalisisAbcUseCase {

    private static final Logger log = LoggerFactory.getLogger(EjecutarAnalisisAbcService.class);

    private static final BigDecimal UMBRAL_A = new BigDecimal("0.80");
    private static final BigDecimal UMBRAL_B = new BigDecimal("0.95");

    private final ClasificacionProductoRepository clasificacionRepository;
    private final MetricaMovimientoQueryPort metricaMovimientoQueryPort;
    private final TenantProviderPort tenantProviderPort;
    private final ApplicationEventPublisher eventPublisher;

    public EjecutarAnalisisAbcService(
            ClasificacionProductoRepository clasificacionRepository,
            MetricaMovimientoQueryPort metricaMovimientoQueryPort,
            TenantProviderPort tenantProviderPort,
            ApplicationEventPublisher eventPublisher) {
        this.clasificacionRepository = Objects.requireNonNull(clasificacionRepository, "ClasificacionProductoRepository es obligatorio");
        this.metricaMovimientoQueryPort = Objects.requireNonNull(metricaMovimientoQueryPort, "MetricaMovimientoQueryPort es obligatorio");
        this.tenantProviderPort = Objects.requireNonNull(tenantProviderPort, "TenantProviderPort es obligatorio");
        this.eventPublisher = Objects.requireNonNull(eventPublisher, "ApplicationEventPublisher es obligatorio");
    }

    @Override
    @Transactional
    public AnalisisAbcResponse ejecutar(EjecutarAnalisisAbcCommand command) {
        Objects.requireNonNull(command, "EjecutarAnalisisAbcCommand no puede ser nulo");

        // 1. Extraer empresaId de forma segura (MT-01)
        UUID empresaId = tenantProviderPort.getEmpresaIdAutenticada().valor();
        EmpresaId domainEmpresaId = new EmpresaId(empresaId);
        BodegaId domainBodegaId = new BodegaId(command.bodegaId());

        log.info("Iniciando Análisis ABC para Empresa [{}] y Bodega [{}]", empresaId, command.bodegaId());

        // 2. Obtener métricas históricas de salida desde el puerto de consulta
        List<ProductoMetricaSalidaDto> metricas = metricaMovimientoQueryPort
                .obtenerMetricasSalidaPorBodega(empresaId, command.bodegaId());

        if (metricas.isEmpty()) {
            log.warn("No se encontraron métricas de productos para la bodega [{}] de la empresa [{}]",
                    command.bodegaId(), empresaId);
            return new AnalisisAbcResponse(
                    command.bodegaId(),
                    empresaId,
                    0,
                    0,
                    0,
                    0,
                    Instant.now(),
                    List.of()
            );
        }

        // 3. Calcular umbrales de Pareto
        List<ProductoConCategoria> clasificados = calcularPareto(metricas);

        int totalA = 0;
        int totalB = 0;
        int totalC = 0;
        List<AnalisisAbcResponse.DetalleClasificacionDto> detalles = new ArrayList<>();

        // 4. Actualizar agregados de dominio, persistir y publicar eventos
        for (ProductoConCategoria item : clasificados) {
            ProductoId domainProductoId = new ProductoId(item.metrica.productoId());

            ClasificacionProducto clasificacion = clasificacionRepository
                    .buscarPorProducto(domainEmpresaId, domainBodegaId, domainProductoId)
                    .orElseGet(() -> ClasificacionProducto.iniciar(domainEmpresaId, domainBodegaId, domainProductoId));

            MetricaMovimiento metricaVo = MetricaMovimiento.de(
                    item.metrica.frecuenciaSalida(),
                    item.metrica.valorTotalDespachado()
            );

            // Reclasificar según regla de negocio
            clasificacion.reclasificar(metricaVo, item.categoria);

            // Guardar en el repositorio
            clasificacionRepository.guardar(clasificacion);

            // Publicar eventos si hubo cambio de categoría
            List<ProductoReclasificadoEvent> eventos = clasificacion.pullDomainEvents();
            eventos.forEach(event -> {
                log.info("Publicando ProductoReclasificadoEvent: producto={}, {} -> {}",
                        event.productoId().valor(), event.categoriaAnterior(), event.categoriaNueva());
                eventPublisher.publishEvent(event);
            });

            if (item.categoria == CategoriaABC.A) totalA++;
            else if (item.categoria == CategoriaABC.B) totalB++;
            else if (item.categoria == CategoriaABC.C) totalC++;

            detalles.add(new AnalisisAbcResponse.DetalleClasificacionDto(
                    item.metrica.productoId(),
                    item.categoria.name(),
                    item.metrica.frecuenciaSalida(),
                    item.metrica.valorTotalDespachado()
            ));
        }

        log.info("Análisis ABC completado para Bodega [{}]: Total={}, A={}, B={}, C={}",
                command.bodegaId(), clasificados.size(), totalA, totalB, totalC);

        return new AnalisisAbcResponse(
                command.bodegaId(),
                empresaId,
                clasificados.size(),
                totalA,
                totalB,
                totalC,
                Instant.now(),
                detalles
        );
    }

    private List<ProductoConCategoria> calcularPareto(List<ProductoMetricaSalidaDto> metricas) {
        BigDecimal totalValor = metricas.stream()
                .map(ProductoMetricaSalidaDto::valorTotalDespachado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int totalFrecuencia = metricas.stream()
                .mapToInt(ProductoMetricaSalidaDto::frecuenciaSalida)
                .sum();

        List<ProductoMetricaSalidaDto> listaOrdenada = new ArrayList<>(metricas);
        List<ProductoConCategoria> resultado = new ArrayList<>();

        if (totalValor.compareTo(BigDecimal.ZERO) > 0) {
            // Ordenar descendentemente por valor total despachado y luego frecuencia
            listaOrdenada.sort(Comparator
                    .comparing(ProductoMetricaSalidaDto::valorTotalDespachado, Comparator.reverseOrder())
                    .thenComparing(ProductoMetricaSalidaDto::frecuenciaSalida, Comparator.reverseOrder())
            );

            BigDecimal acumulado = BigDecimal.ZERO;
            for (ProductoMetricaSalidaDto m : listaOrdenada) {
                if (m.frecuenciaSalida() == 0 && m.valorTotalDespachado().compareTo(BigDecimal.ZERO) == 0) {
                    resultado.add(new ProductoConCategoria(m, CategoriaABC.C));
                    continue;
                }

                BigDecimal pctInicio = acumulado.divide(totalValor, 6, RoundingMode.HALF_UP);
                acumulado = acumulado.add(m.valorTotalDespachado());

                CategoriaABC categoria;
                if (pctInicio.compareTo(UMBRAL_A) < 0) {
                    categoria = CategoriaABC.A;
                } else if (pctInicio.compareTo(UMBRAL_B) < 0) {
                    categoria = CategoriaABC.B;
                } else {
                    categoria = CategoriaABC.C;
                }
                resultado.add(new ProductoConCategoria(m, categoria));
            }
        } else if (totalFrecuencia > 0) {
            // Ordenar descendentemente por frecuencia de salida
            listaOrdenada.sort(Comparator
                    .comparing(ProductoMetricaSalidaDto::frecuenciaSalida, Comparator.reverseOrder())
            );

            int acumuladoFreq = 0;
            for (ProductoMetricaSalidaDto m : listaOrdenada) {
                if (m.frecuenciaSalida() == 0) {
                    resultado.add(new ProductoConCategoria(m, CategoriaABC.C));
                    continue;
                }

                double pctInicio = (double) acumuladoFreq / totalFrecuencia;
                acumuladoFreq += m.frecuenciaSalida();

                CategoriaABC categoria;
                if (pctInicio < 0.80) {
                    categoria = CategoriaABC.A;
                } else if (pctInicio < 0.95) {
                    categoria = CategoriaABC.B;
                } else {
                    categoria = CategoriaABC.C;
                }
                resultado.add(new ProductoConCategoria(m, categoria));
            }
        } else {
            // Sin movimientos: todos a C
            for (ProductoMetricaSalidaDto m : listaOrdenada) {
                resultado.add(new ProductoConCategoria(m, CategoriaABC.C));
            }
        }

        return resultado;
    }

    private record ProductoConCategoria(ProductoMetricaSalidaDto metrica, CategoriaABC categoria) {}
}
