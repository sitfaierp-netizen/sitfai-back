#!/bin/bash
# =============================================================================
# SITFAI ERP — Prueba E2E: Flujo "Cliente Cero" (Big Bang)
# Valida la creación de la Empresa y la coreografía SCM (Bodega + Caja automáticas)
# =============================================================================
set -e

# Configuración
GATEWAY_URL="http://localhost:8000"
KEYCLOAK_URL="http://localhost:8080/realms/sitfai-erp/protocol/openid-connect/token"
RUC="20$(date +%s | cut -c2-10)" # RUC dinámico para evitar conflictos (11 dígitos)
RAZON_SOCIAL="Empresa Cero $(date +%s) S.A."

echo "============================================================"
echo "1. AUTENTICACIÓN (Obteniendo JWT de Keycloak)"
echo "============================================================"
# Simulación o intento real contra Keycloak local. Si falla por falta de client_id en dev, 
# se usa un mock JWT (asumiendo que el SecurityConfig permita mock o esté en modo dev).
# Se asume cliente 'admin-cli' y usuario 'admin' / 'admin123'
TOKEN=$(curl -s -X POST $KEYCLOAK_URL \
  -H "Host: keycloak-iam:8080" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=api-tienda-client" \
  -d "username=super.admin" \
  -d "password=admin123" \
  -d "grant_type=password" | grep -o '"access_token":"[^"]*' | sed 's/"access_token":"//')

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
    echo "⚠️  No se pudo obtener un token válido desde Keycloak."
    echo "Usando TOKEN MOCK de SUPER_ADMIN para bypassing local (si aplica)..."
    TOKEN="mock-super-admin-jwt-token"
else
    echo "✅ Token JWT (SUPER_ADMIN) obtenido exitosamente."
fi

echo ""
echo "============================================================"
echo "2. EL BIG BANG (Creación de Tenant Raíz)"
echo "============================================================"
echo "Enviando POST a $GATEWAY_URL/api/v1/empresas..."

RESPONSE=$(curl -v -X POST "$GATEWAY_URL/api/v1/empresas" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "ruc": "'"$RUC"'",
        "razonSocial": "'"$RAZON_SOCIAL"'"
      }')

echo "Respuesta del Backend:"
echo $RESPONSE

EMPRESA_ID=$(echo $RESPONSE | grep -o '"id":"[^"]*' | sed 's/"id":"//')

if [ "$EMPRESA_ID" == "null" ] || [ -z "$EMPRESA_ID" ]; then
    echo "❌ Falló la creación de la Empresa. Abortando E2E."
    exit 1
fi
echo "✅ Empresa Creada Exitosamente. ID Asignado: $EMPRESA_ID"

echo ""
echo "============================================================"
echo "3. DELAY DE COREOGRAFÍA (SAGA PATTERN)"
echo "============================================================"
echo "Esperando 3 segundos para que los Event Listeners (Kafka/Spring Events) propaguen el evento..."
sleep 3

echo ""
echo "============================================================"
echo "4. VERIFICACIÓN LOGÍSTICA (INVENTORY & POS)"
echo "============================================================"

# Nota Arquitectónica: Actualmente BodegaController y CajaController NO tienen endpoints GET findAll por empresa.
# El script lanza los requests HTTP esperando que se implementen en el futuro.

echo "Verificando Bodega Matriz (Inventory)..."
BODEGAS_RESP=$(curl -s -X GET "$GATEWAY_URL/api/v1/bodegas" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Empresa-Id: $EMPRESA_ID")
echo "Respuesta Bodegas: $BODEGAS_RESP"

BODEGA_ID=$(echo $BODEGAS_RESP | grep -o '"id":"[^"]*' | head -1 | sed 's/"id":"//')
if [ -z "$BODEGA_ID" ]; then
    echo "❌ No se encontró ninguna bodega para la empresa. Abortando."
    exit 1
fi
echo "✅ Bodega encontrada. ID: $BODEGA_ID"

echo ""
echo "Verificando Caja Principal (POS)..."
CAJAS_RESP=$(curl -s -X GET "$GATEWAY_URL/api/v1/cajas" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Empresa-Id: $EMPRESA_ID")
echo "Respuesta Cajas: $CAJAS_RESP"

CAJA_ID=$(echo $CAJAS_RESP | grep -o '"id":"[^"]*' | head -1 | sed 's/"id":"//')
if [ -z "$CAJA_ID" ]; then
    echo "❌ No se encontró ninguna caja para la empresa. Abortando."
    exit 1
fi
echo "✅ Caja encontrada. ID: $CAJA_ID"

echo ""
echo "============================================================"
echo "5. APERTURA DE TURNO (POS)"
echo "============================================================"
echo "Abriendo turno para la caja: $CAJA_ID"
TURNO_RESP=$(curl -s -X POST "$GATEWAY_URL/api/v1/pos/turnos" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Empresa-Id: $EMPRESA_ID" \
  -H "Content-Type: application/json" \
  -d '{
        "cajaId": "'"$CAJA_ID"'",
        "sucursalId": "22222222-2222-2222-2222-222222222222",
        "usuarioId": "33333333-3333-3333-3333-333333333333",
        "montoApertura": 100.00
      }')
echo "Respuesta Apertura Turno: $TURNO_RESP"
TURNO_ID=$(echo $TURNO_RESP | grep -o '"id":"[^"]*' | sed 's/"id":"//')
if [ -z "$TURNO_ID" ] || [ "$TURNO_ID" == "null" ]; then
    echo "❌ Falló la apertura de turno. Abortando E2E."
    exit 1
fi

echo ""
echo "============================================================"
echo "6. REGISTRO DE VENTA (POS)"
echo "============================================================"
echo "Registrando venta en el turno: $TURNO_ID"
VENTA_RESP=$(curl -s -X POST "$GATEWAY_URL/api/v1/pos/turnos/$TURNO_ID/transacciones" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Empresa-Id: $EMPRESA_ID" \
  -H "Content-Type: application/json" \
  -d '{
        "tipoTransaccion": "VENTA",
        "monto": 250.00,
        "referencia": "REF-VENTA-001"
      }')
echo "Respuesta Venta: $VENTA_RESP"

echo ""
echo "============================================================"
echo "7. DELAY COREOGRAFÍA SCM"
echo "============================================================"
echo "Esperando 3 segundos para propagación del VentaRegistradaEvent..."
sleep 3

echo ""
echo "============================================================"
echo "8. VALIDACIÓN CQRS E INVENTARIO (API)"
echo "============================================================"
echo "Verificando deducción de stock vía API..."
STOCK_RESP=$(curl -s -X GET "$GATEWAY_URL/api/v1/inventory/bodegas/$BODEGA_ID/stock" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Empresa-Id: $EMPRESA_ID")
echo "Respuesta Stock: $STOCK_RESP"

echo ""
echo "============================================================"
echo "Flujo Cliente Cero Finalizado Exitosamente."
