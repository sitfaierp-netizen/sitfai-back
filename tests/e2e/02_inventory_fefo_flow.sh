#!/bin/bash

# ==============================================================================
# SITFAI ERP - Auditoría E2E: Flujo FEFO de Inventarios
# ==============================================================================

set -e

# Configuración de Endpoints y Credenciales
KEYCLOAK_URL="http://keycloak-iam:8080/realms/sitfai-erp/protocol/openid-connect/token"
CLIENT_ID="api-tienda-client"
USERNAME="bodega_test"
PASSWORD="test1234"

GATEWAY_URL="http://sitfai-gateway:8000/api/v1/inventory"
BODEGA_ID="f2bbf0e1-5972-42a2-9a62-effedc45f4b4"
PRODUCTO_ID="88888888-8888-8888-8888-888888888888"

echo "========================================================"
echo " INICIANDO AUDITORÍA E2E: INVENTORY FEFO FLOW"
echo "========================================================"

# ------------------------------------------------------------------------------
# 1. Autenticación (Keycloak)
# ------------------------------------------------------------------------------
echo -n "[1/6] Obteniendo Token JWT de Keycloak para usuario $USERNAME... "
TOKEN_RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL" \
  -d "client_id=$CLIENT_ID" \
  -d "username=$USERNAME" \
  -d "password=$PASSWORD" \
  -d "grant_type=password")

ACCESS_TOKEN=$(echo $TOKEN_RESPONSE | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

if [ "$ACCESS_TOKEN" == "null" ] || [ -z "$ACCESS_TOKEN" ]; then
    echo "FALLO: No se pudo obtener el token JWT."
    echo $TOKEN_RESPONSE
    exit 1
fi
echo "OK"

# ------------------------------------------------------------------------------
# 2. Ingreso 1: Lote con caducidad lejana (2026-12-01)
# ------------------------------------------------------------------------------
echo -n "[2/6] Ejecutando INGRESO 1 (Cantidad: 10, Vence: 2026-12-01T00:00:00Z)... "

INGRESO1_PAYLOAD=$(cat <<EOF
{
  "productoId": "$PRODUCTO_ID",
  "cantidad": 10.0,
  "loteId": "LOTE-DIC-2026",
  "fechaCaducidad": "2026-12-01T00:00:00Z",
  "docFuenteTipo": "COMPRA",
  "docFuenteNumero": "OC-001"
}
EOF
)

STATUS_ING1=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$GATEWAY_URL/bodegas/$BODEGA_ID/ingresos" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$INGRESO1_PAYLOAD")

if [[ "$STATUS_ING1" != 20* ]]; then
    echo "FALLO: HTTP $STATUS_ING1"
    curl -s -X POST "$GATEWAY_URL/bodegas/$BODEGA_ID/ingresos" \
      -H "Authorization: Bearer $ACCESS_TOKEN" \
      -H "Content-Type: application/json" \
      -d "$INGRESO1_PAYLOAD"
    exit 1
fi
echo "OK (HTTP $STATUS_ING1)"

# ------------------------------------------------------------------------------
# 3. Ingreso 2: Lote con caducidad cercana (2026-10-01)
# ------------------------------------------------------------------------------
echo -n "[3/6] Ejecutando INGRESO 2 (Cantidad: 5, Vence: 2026-10-01T00:00:00Z)... "

INGRESO2_PAYLOAD=$(cat <<EOF
{
  "productoId": "$PRODUCTO_ID",
  "cantidad": 5.0,
  "loteId": "LOTE-OCT-2026",
  "fechaCaducidad": "2026-10-01T00:00:00Z",
  "docFuenteTipo": "COMPRA",
  "docFuenteNumero": "OC-002"
}
EOF
)

STATUS_ING2=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$GATEWAY_URL/bodegas/$BODEGA_ID/ingresos" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$INGRESO2_PAYLOAD")

if [[ "$STATUS_ING2" != 20* ]]; then
    echo "FALLO: HTTP $STATUS_ING2"
    curl -s -X POST "$GATEWAY_URL/bodegas/$BODEGA_ID/ingresos" \
      -H "Authorization: Bearer $ACCESS_TOKEN" \
      -H "Content-Type: application/json" \
      -d "$INGRESO2_PAYLOAD"
    exit 1
fi
echo "OK (HTTP $STATUS_ING2)"

# ------------------------------------------------------------------------------
# 4. Egreso FEFO: Descuento que debería tomar prioridad del Lote Octubre
# ------------------------------------------------------------------------------
echo -n "[4/6] Ejecutando EGRESO FEFO (Cantidad: 7)... "

EGRESO_PAYLOAD=$(cat <<EOF
{
  "productoId": "$PRODUCTO_ID",
  "cantidad": 7.0,
  "docFuenteTipo": "VENTA",
  "docFuenteNumero": "VEN-001"
}
EOF
)

STATUS_EGR1=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$GATEWAY_URL/bodegas/$BODEGA_ID/egresos" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$EGRESO_PAYLOAD")

if [[ "$STATUS_EGR1" != 20* ]]; then
    echo "FALLO: HTTP $STATUS_EGR1"
    curl -s -X POST "$GATEWAY_URL/bodegas/$BODEGA_ID/egresos" \
      -H "Authorization: Bearer $ACCESS_TOKEN" \
      -H "Content-Type: application/json" \
      -d "$EGRESO_PAYLOAD"
    exit 1
fi
echo "OK (HTTP $STATUS_EGR1)"

# ------------------------------------------------------------------------------
# 5. Validación de Reglas: Exceso de Stock
# ------------------------------------------------------------------------------
echo -n "[5/6] Ejecutando EGRESO EXCESIVO (Cantidad: 100)... "

EGRESO_EXCESO_PAYLOAD=$(cat <<EOF
{
  "productoId": "$PRODUCTO_ID",
  "cantidad": 100.0,
  "docFuenteTipo": "VENTA",
  "docFuenteNumero": "VEN-002"
}
EOF
)

STATUS_EXCESO=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$GATEWAY_URL/bodegas/$BODEGA_ID/egresos" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$EGRESO_EXCESO_PAYLOAD")

if [[ "$STATUS_EXCESO" == 20* ]]; then
    echo "FALLO CRÍTICO: El servidor permitió un egreso sin stock (HTTP $STATUS_EXCESO)."
    exit 1
fi
echo "OK, Bloqueado correctamente (HTTP $STATUS_EXCESO)"
echo "=> Respuesta de Error del Servidor:"
curl -s -X POST "$GATEWAY_URL/bodegas/$BODEGA_ID/egresos" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "$EGRESO_EXCESO_PAYLOAD"

# ------------------------------------------------------------------------------
# 6. Seguridad: Intento sin Token JWT
# ------------------------------------------------------------------------------
echo -n "[6/6] Ejecutando petición SIN TOKEN JWT... "

STATUS_UNAUTH=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$GATEWAY_URL/bodegas/$BODEGA_ID/egresos" \
  -H "Content-Type: application/json" \
  -d "$EGRESO_PAYLOAD")

if [[ "$STATUS_UNAUTH" != 401 && "$STATUS_UNAUTH" != 403 ]]; then
    echo "FALLO: Se esperaba HTTP 401/403, pero se recibió HTTP $STATUS_UNAUTH"
    exit 1
fi
echo "OK, Denegado (HTTP $STATUS_UNAUTH)"

echo "========================================================"
echo " AUDITORÍA E2E COMPLETADA CON ÉXITO "
echo "========================================================"
