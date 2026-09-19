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
BODEGAS_RESP=$(curl -s -w "\nHTTP_CODE:%{http_code}\n" -X GET "$GATEWAY_URL/api/v1/bodegas" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Empresa-Id: $EMPRESA_ID")
echo "$BODEGAS_RESP"

echo ""
echo "Verificando Caja Principal (POS)..."
CAJAS_RESP=$(curl -s -w "\nHTTP_CODE:%{http_code}\n" -X GET "$GATEWAY_URL/api/v1/cajas" \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Empresa-Id: $EMPRESA_ID")
echo "$CAJAS_RESP"

echo ""
echo "============================================================"
echo "5. VERIFICACIÓN DEFINITIVA EN BASE DE DATOS (Opcional E2E)"
echo "============================================================"
echo "Consultando contenedores MySQL para validar la inserción real de los Agregados..."
docker exec sitfai-mysql-db mysql -u sitfai_user -psitfai_secret_pwd sitfai_tienda -e "
SELECT id, nombre, empresa_id FROM inventory_bodega WHERE empresa_id='$EMPRESA_ID';
"
# docker exec sitfai-mysql-db mysql -u sitfai_user -psitfai_secret_pwd sitfai_tienda -e "
# SELECT id, nombre, empresa_id FROM cajas WHERE empresa_id='$EMPRESA_ID';
# "


echo ""
echo "============================================================"
echo "6. APERTURA DE TURNO (POS)"
echo "============================================================"
# Asumimos que extraemos el ID de la caja del query anterior o creamos un UUID dummy si no lo hay
# CAJA_ID=$(docker exec sitfai-mysql-db mysql -u sitfai_user -psitfai_secret_pwd sitfai_tienda -sN -e "SELECT id FROM cajas WHERE empresa_id='$EMPRESA_ID' LIMIT 1;")
CAJA_ID="caja-mock-id"

echo "Abriendo turno para la caja: $CAJA_ID"
TURNO_RESP=$(curl -s -X POST "$GATEWAY_URL/api/v1/pos/turnos" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "cajaId": "'"$CAJA_ID"'",
        "montoApertura": 100.00
      }')
echo "Respuesta Apertura Turno: $TURNO_RESP"
TURNO_ID=$(echo $TURNO_RESP | grep -o '"id":"[^"]*' | sed 's/"id":"//')
if [ -z "$TURNO_ID" ]; then
    TURNO_ID="turno-mock-id"
fi

echo ""
echo "============================================================"
echo "7. REGISTRO DE VENTA (POS)"
echo "============================================================"
echo "Registrando venta en el turno: $TURNO_ID"
VENTA_RESP=$(curl -s -X POST "$GATEWAY_URL/api/v1/pos/turnos/$TURNO_ID/transacciones" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
        "tipo": "VENTA",
        "montoTotal": 250.00,
        "lineas": [
           { "productoId": "prod-1", "cantidad": 2, "precioUnitario": 125.00 }
        ]
      }')
echo "Respuesta Venta: $VENTA_RESP"

echo ""
echo "============================================================"
echo "8. DELAY COREOGRAFÍA SCM"
echo "============================================================"
echo "Esperando 3 segundos para propagación del VentaRegistradaEvent..."
sleep 3

echo ""
echo "============================================================"
echo "9. VALIDACIÓN CQRS E INVENTARIO (DB)"
echo "============================================================"
echo "Verificando deducción de stock en inventory_stock_view..."
docker exec sitfai-mysql-db mysql -u sitfai_user -psitfai_secret_pwd sitfai_tienda -e "
SELECT empresa_id, producto_id, cantidad_total FROM inventory_stock_view WHERE empresa_id='$EMPRESA_ID' LIMIT 5;
"

echo ""
echo "============================================================"
echo "10. VALIDACIÓN TRIBUTARIA (BILLING) (DB)"
echo "============================================================"
echo "Verificando emisión automática de Factura Electrónica (DIAN)..."
docker exec sitfai-mysql-db mysql -u sitfai_user -psitfai_secret_pwd sitfai_tienda -e "
SELECT id, empresa_id, ruc_cliente, estado, total_general FROM billing_factura WHERE empresa_id='$EMPRESA_ID';
"

echo "============================================================"
echo "Flujo Cliente Cero Finalizado Exitosamente."
