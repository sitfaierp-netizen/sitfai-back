package com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.service;

import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.dto.SucursalResponse;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.EmpresaRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.application.port.output.SucursalRepository;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.exception.EmpresaNoEncontradaException;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.EstadoSucursal;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.model.Sucursal;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.EmpresaId;
import com.SITFAI_CORE_ERP_TIENDA.core_empresa.domain.valueobject.SucursalId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit Tests para {@link ObtenerSucursalesPorEmpresaService} (Regla 8 — Application Layer: JUnit5 + Mockito).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ObtenerSucursalesPorEmpresaService — Tests de la capa de Aplicación")
class ObtenerSucursalesPorEmpresaServiceTest {

    @Mock
    private EmpresaRepository empresaRepository;

    @Mock
    private SucursalRepository sucursalRepository;

    private ObtenerSucursalesPorEmpresaService sut;

    @BeforeEach
    void setUp() {
        sut = new ObtenerSucursalesPorEmpresaService(empresaRepository, sucursalRepository);
    }

    @Test
    @DisplayName("Debe retornar lista de SucursalResponse cuando la Empresa existe (MT-02)")
    void ejecutar_conEmpresaExistente_retornaListaDeSucursales() {
        // GIVEN
        UUID empresaIdUuid = UUID.randomUUID();
        EmpresaId empresaId = EmpresaId.de(empresaIdUuid);
        Sucursal sucursal = Sucursal.reconstituir(
                SucursalId.generar(), "MATRIZ", "Sucursal Matriz",
                EstadoSucursal.ACTIVA, Instant.now(), Instant.now()
        );

        when(empresaRepository.existe(empresaId)).thenReturn(true);
        when(sucursalRepository.buscarPorEmpresaId(empresaId)).thenReturn(List.of(sucursal));

        // WHEN
        List<SucursalResponse> resultado = sut.ejecutar(empresaIdUuid);

        // THEN
        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).codigo()).isEqualTo("MATRIZ");
        assertThat(resultado.get(0).estado()).isEqualTo("ACTIVA");
        verify(empresaRepository).existe(empresaId);
        verify(sucursalRepository).buscarPorEmpresaId(empresaId);
    }

    @Test
    @DisplayName("Debe retornar lista vacía cuando la Empresa no tiene Sucursales")
    void ejecutar_conEmpresaSinSucursales_retornaListaVacia() {
        // GIVEN
        UUID empresaIdUuid = UUID.randomUUID();
        EmpresaId empresaId = EmpresaId.de(empresaIdUuid);

        when(empresaRepository.existe(empresaId)).thenReturn(true);
        when(sucursalRepository.buscarPorEmpresaId(empresaId)).thenReturn(List.of());

        // WHEN
        List<SucursalResponse> resultado = sut.ejecutar(empresaIdUuid);

        // THEN
        assertThat(resultado).isEmpty();
    }

    @Test
    @DisplayName("Debe lanzar EmpresaNoEncontradaException si la Empresa no existe (MT-02 fail-fast)")
    void ejecutar_conEmpresaInexistente_lanzaEmpresaNoEncontradaException() {
        // GIVEN
        UUID empresaIdUuid = UUID.randomUUID();
        EmpresaId empresaId = EmpresaId.de(empresaIdUuid);

        when(empresaRepository.existe(empresaId)).thenReturn(false);

        // WHEN / THEN
        assertThatThrownBy(() -> sut.ejecutar(empresaIdUuid))
                .isInstanceOf(EmpresaNoEncontradaException.class);
    }

    @Test
    @DisplayName("Debe lanzar NullPointerException si empresaId es null")
    void ejecutar_conEmpresaIdNull_lanzaNullPointerException() {
        assertThatThrownBy(() -> sut.ejecutar(null))
                .isInstanceOf(NullPointerException.class);
    }
}
