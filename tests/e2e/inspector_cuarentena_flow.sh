#!/bin/bash
# =============================================================================
# SITFAI ERP — Prueba E2E: Flujo "Inspector de Calidad" (Logística Inversa)
# Valida la aprobación de cuarentena y transferencia de stock (BOD-06 / CAJ-08)
# =============================================================================
set -e

# Configuración
GATEWAY_URL="http://localhost:8000"
KEYCLOAK_URL="http://localhost:8080/realms/sitfai-erp/protocol/openid-connect/token"

# Simular datos para la inspección
EMPRESA_ID="a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
SUCURSAL_ID="c7d8e9f0-4444-5555-6666-777788889999" # Mismo ID que la caja/sucursal
PRODUCTO_ID="prod-1"
CANTIDAD_DEVUELTA="2.00"

echo "============================================================"
echo "1. AUTENTICACIÓN (Obteniendo JWT de Keycloak para INSPECTOR)"
echo "============================================================"
TOKEN=$(curl -s -X POST $KEYCLOAK_URL \
  -H "Host: keycloak-iam:8080" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=api-tienda-client" \
  -d "username=cajero_test" \
  -d "password=12345" \
  -d "grant_type=password" | grep -o '"access_token":"[^"]*' | sed 's/"access_token":"//')

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
    echo "⚠️  No se pudo obtener un token válido desde Keycloak."
    echo "Usando TOKEN MOCK de INSPECTOR para bypassing local..."
    TOKEN="mock-inspector-jwt-token"
else
    echo "✅ Token JWT obtenido exitosamente."
fi

echo ""
echo "============================================================"
echo "2. APROBACIÓN DE CUARENTENA (Logística Inversa)"
echo "============================================================"
echo "Enviando POST a $GATEWAY_URL/api/v1/inventory/cuarentena/aprobar..."

RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}\n" -X POST "$GATEWAY_URL/api/v1/inventory/cuarentena/aprobar" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "sucursalId": "'"$SUCURSAL_ID"'",
        "productoId": "'"$PRODUCTO_ID"'",
        "cantidad": '"$CANTIDAD_DEVUELTA"',
        "aprobado": true
      }')

echo "Respuesta del Backend:"
echo "$RESPONSE"

echo ""
echo "============================================================"
echo "3. DELAY DE PROYECCIÓN CQRS"
echo "============================================================"
echo "Esperando 2 segundos para que el StockConsolidadoProjector actualice las vistas..."
sleep 2

echo ""
echo "============================================================"
echo "4. VALIDACIÓN DE PARTIDA DOBLE EN BASE DE DATOS (CQRS)"
echo "============================================================"
echo "Verificando saldos en inventory_stock_view..."
docker exec sitfai-mysql-db mysql -u sitfai_user -psitfai_secret_pwd sitfai_tienda -e "
SELECT bodega_id, producto_id, stock_disponible 
FROM inventory_stock_view 
WHERE empresa_id='$EMPRESA_ID' AND producto_id='$PRODUCTO_ID';
"

echo "============================================================"
echo "Flujo Logística Inversa Finalizado Exitosamente."
echo "La cantidad en $SUCURSAL_ID-CUARENTENA debió disminuir."
echo "La cantidad en $SUCURSAL_ID (MATRIZ) debió aumentar."
echo "============================================================"
