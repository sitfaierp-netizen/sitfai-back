package com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.service;

import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.Bodega;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.model.TipoMovimiento;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.Cantidad;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.DocumentoFuenteId;
import com.SITFAI_CORE_ERP_TIENDA.Api_Tienda.inventory.domain.valueobject.ProductoId;

import java.util.Objects;

/**
 * Domain Service: Gestiona las transferencias de stock entre bodegas.
 * <p>
 * REGLA-1 / REGLA-3: Clase Java pura, sin dependencias de frameworks (@Service, JPA, etc.).
 * Implementa la lógica transversal que involucra a múltiples instancias del Agregado Bodega.
 */
public class TransferenciaStockDomainService {

    /**
     * Transfiere stock de una bodega origen a una bodega destino.
     * <p>
     * INVARIANTES:
     * - Ambas bodegas deben pertenecer al mismo Tenant (MT-01).
     * - La bodega origen no puede quedar con stock negativo tras la salida (BOD-05 validada por Bodega).
     * - La transferencia genera un movimiento de SALIDA y otro de ENTRADA (BOD-06).
     *
     * @param origen          Bodega desde la cual sale el stock.
     * @param destino         Bodega hacia la cual ingresa el stock.
     * @param producto        Producto a transferir.
     * @param cantidad        Cantidad a transferir.
     * @param documentoFuente Número de documento de la transferencia (BOD-04).
     */
    public void transferir(
            Bodega origen, 
            Bodega destino, 
            ProductoId producto, 
            Cantidad cantidad, 
            String documentoFuente) {
        
        Objects.requireNonNull(origen, "Bodega de origen es obligatoria.");
        Objects.requireNonNull(destino, "Bodega de destino es obligatoria.");
        Objects.requireNonNull(producto, "Producto es obligatorio.");
        Objects.requireNonNull(cantidad, "Cantidad es obligatoria.");
        if (documentoFuente == null || documentoFuente.isBlank()) {
            throw new IllegalArgumentException("El documento fuente de transferencia es obligatorio.");
        }

        // 1. Validar que no se transfiera a sí misma
        if (origen.getId().equals(destino.getId())) {
            throw new IllegalArgumentException("La bodega de origen y destino no pueden ser la misma.");
        }

        // 2. Validar pertenencia al mismo Tenant (MT-01)
        if (!origen.getEmpresaId().equals(destino.getEmpresaId())) {
            throw new IllegalStateException("Transferencia denegada: Las bodegas pertenecen a diferentes tenants.");
        }

        // Crear el identificador de documento fuente tipo TRANSFERENCIA
        DocumentoFuenteId docId = new DocumentoFuenteId("TRANSFERENCIA", documentoFuente);

        // 3. Ejecutar movimientos (El orden asegura el fail-fast si origen no tiene stock suficiente, BOD-05)
        origen.registrarMovimiento(producto, cantidad, TipoMovimiento.SALIDA, docId);
        destino.registrarMovimiento(producto, cantidad, TipoMovimiento.ENTRADA, docId);
    }
}
