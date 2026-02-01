#!/bin/bash

# Script para generar certificados SSL/TLS para gRPC
# Genera keystore para el servidor y truststore para el cliente

set -e

VALIDITY=365
PASSWORD="changeit"
KEYSIZE=2048
CERTS_DIR="src/main/resources"

echo "======================================"
echo "Generando certificados SSL/TLS"
echo "======================================"

# Crear directorio para certificados si no existe
mkdir -p "$CERTS_DIR"

# Limpiar certificados anteriores si existen
echo "Limpiando certificados anteriores..."
rm -f "$CERTS_DIR/keystore.jks"
rm -f "$CERTS_DIR/truststore.jks"
rm -f "$CERTS_DIR/server.crt"

echo ""
echo "1. Generando keystore del servidor..."
keytool -genkeypair \
  -alias server \
  -keyalg RSA \
  -keysize $KEYSIZE \
  -validity $VALIDITY \
  -keystore "$CERTS_DIR/keystore.jks" \
  -storepass $PASSWORD \
  -keypass $PASSWORD \
  -dname "CN=localhost,OU=Development,O=gRPC Course,L=City,ST=State,C=US"

echo ""
echo "2. Exportando certificado público..."
keytool -exportcert \
  -alias server \
  -keystore "$CERTS_DIR/keystore.jks" \
  -storepass $PASSWORD \
  -file "$CERTS_DIR/server.crt"

echo ""
echo "3. Creando truststore del cliente..."
keytool -importcert \
  -alias server \
  -file "$CERTS_DIR/server.crt" \
  -keystore "$CERTS_DIR/truststore.jks" \
  -storepass $PASSWORD \
  -noprompt

echo ""
echo "======================================"
echo "✓ Certificados generados exitosamente"
echo "======================================"
echo ""
echo "Archivos creados en $CERTS_DIR:"
echo "  - keystore.jks (para el servidor gRPC)"
echo "  - truststore.jks (para el cliente gRPC)"
echo "  - server.crt (certificado público)"
echo ""
echo "Password: $PASSWORD"
echo "Validez: $VALIDITY días"
echo ""
