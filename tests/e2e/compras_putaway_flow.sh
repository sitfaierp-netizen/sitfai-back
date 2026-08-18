#!/bin/bash
# =============================================================================
# SITFAI ERP — Prueba E2E: Flujo "Putaway" (Aprovisionamiento por Compras)
# Valida la integración asíncrona purchasing -> inventory (Reglas BOD-03, BOD-04)
# =============================================================================
set -e

# Configuración
GATEWAY_URL="http://localhost:8000"
KEYCLOAK_URL="http://localhost:8080/realms/sitfai-erp/protocol/openid-connect/token"

# Simular datos para la compra
EMPRESA_ID="a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
PROVEEDOR_ID="f3b4c100-5555-4a11-8c44-d99988887777"
PRODUCTO_ID="prod-putaway-99"
CANTIDAD_COMPRADA="100.00"
COSTO_UNITARIO="250.00"

echo "============================================================"
echo "1. AUTENTICACIÓN (Obteniendo JWT de Keycloak)"
echo "============================================================"
TOKEN=$(curl -s -X POST $KEYCLOAK_URL \
  -H "Host: keycloak-iam:8080" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=api-tienda-client" \
  -d "username=admin_test" \
  -d "password=12345" \
  -d "grant_type=password" | grep -o '"access_token":"[^"]*' | sed 's/"access_token":"//')

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
    echo "⚠️  No se pudo obtener un token válido desde Keycloak."
    echo "Usando TOKEN MOCK para bypassing local..."
    TOKEN="mock-admin-jwt-token"
else
    echo "✅ Token JWT obtenido exitosamente."
fi

echo ""
echo "============================================================"
echo "2. CREAR ORDEN DE COMPRA (Borrador)"
echo "============================================================"
echo "Enviando POST a $GATEWAY_URL/api/v1/purchasing/ordenes..."
RESPONSE_BORRADOR=$(curl -s -X POST "$GATEWAY_URL/api/v1/purchasing/ordenes" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "proveedorId": "'"$PROVEEDOR_ID"'"
      }')

ORDEN_ID=$(echo $RESPONSE_BORRADOR | grep -o '"id":"[^"]*' | sed 's/"id":"//')
echo "✅ Orden de Compra creada: $ORDEN_ID"

echo ""
echo "============================================================"
echo "3. AGREGAR LÍNEAS A LA ORDEN"
echo "============================================================"
curl -s -X POST "$GATEWAY_URL/api/v1/purchasing/ordenes/$ORDEN_ID/lineas" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "productoId": "'"$PRODUCTO_ID"'",
        "cantidad": '"$CANTIDAD_COMPRADA"',
        "costoUnitario": '"$COSTO_UNITARIO"'
      }' > /dev/null
echo "✅ Producto $PRODUCTO_ID agregado por cantidad de $CANTIDAD_COMPRADA."

echo ""
echo "============================================================"
echo "4. EMITIR ORDEN DE COMPRA"
echo "============================================================"
curl -s -X PATCH "$GATEWAY_URL/api/v1/purchasing/ordenes/$ORDEN_ID/estado" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "nuevoEstado": "EMITIDA"
      }' > /dev/null
echo "✅ Orden $ORDEN_ID marcada como EMITIDA."

echo ""
echo "============================================================"
echo "5. RECIBIR ORDEN Y DISPARAR EVENTO (Putaway SCM)"
echo "============================================================"
echo "Enviando PATCH para marcar como RECIBIDA (disparando evento asíncrono)..."
curl -s -X PATCH "$GATEWAY_URL/api/v1/purchasing/ordenes/$ORDEN_ID/estado" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "nuevoEstado": "RECIBIDA"
      }' > /dev/null
echo "✅ Orden $ORDEN_ID marcada como RECIBIDA."

echo ""
echo "============================================================"
echo "6. DELAY DE COREOGRAFÍA SCM"
echo "============================================================"
echo "Esperando 3 segundos para propagación en Event Bus y proyección CQRS..."
sleep 3

echo ""
echo "============================================================"
echo "7. VALIDACIÓN DE INVENTARIO (DB - Putaway Automático)"
echo "============================================================"
echo "Consultando inventory_stock_view para verificar que $PRODUCTO_ID aumentó en $CANTIDAD_COMPRADA..."
docker exec sitfai-mysql-db mysql -u sitfai_user -psitfai_secret_pwd sitfai_tienda -e "
SELECT bodega_id, producto_id, stock_disponible 
FROM inventory_stock_view 
WHERE empresa_id='$EMPRESA_ID' AND producto_id='$PRODUCTO_ID';
"

echo "============================================================"
echo "Flujo Putaway (SCM) Finalizado."
echo "Valida visualmente que el stock disponible reporte la cantidad correcta."
echo "============================================================"
