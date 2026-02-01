@echo off
REM Script para generar certificados SSL/TLS para gRPC (Windows)
REM Genera keystore para el servidor y truststore para el cliente

setlocal

set VALIDITY=365
set PASSWORD=changeit
set KEYSIZE=2048
set CERTS_DIR=src\main\resources

REM Intentar usar keytool desde el PATH, sino usa la ruta por defecto de Java 21
where keytool >nul 2>&1
if %errorlevel% neq 0 (
    set KEYTOOL="C:\Program Files\Java\jdk-21\bin\keytool.exe"
) else (
    set KEYTOOL=keytool
)

echo ======================================
echo Generando certificados SSL/TLS
echo ======================================

REM Crear directorio para certificados si no existe
if not exist "%CERTS_DIR%" mkdir "%CERTS_DIR%"

REM Limpiar certificados anteriores si existen
echo Limpiando certificados anteriores...
if exist "%CERTS_DIR%\keystore.jks" del /F /Q "%CERTS_DIR%\keystore.jks"
if exist "%CERTS_DIR%\truststore.jks" del /F /Q "%CERTS_DIR%\truststore.jks"
if exist "%CERTS_DIR%\server.crt" del /F /Q "%CERTS_DIR%\server.crt"

echo.
echo 1. Generando keystore del servidor...
%KEYTOOL% -genkeypair ^
  -alias server ^
  -keyalg RSA ^
  -keysize %KEYSIZE% ^
  -validity %VALIDITY% ^
  -keystore "%CERTS_DIR%\keystore.jks" ^
  -storepass %PASSWORD% ^
  -keypass %PASSWORD% ^
  -dname "CN=localhost,OU=Development,O=gRPC Course,L=City,ST=State,C=US"

if errorlevel 1 (
    echo Error generando keystore
    exit /b 1
)

echo.
echo 2. Exportando certificado publico...
%KEYTOOL% -exportcert ^
  -alias server ^
  -keystore "%CERTS_DIR%\keystore.jks" ^
  -storepass %PASSWORD% ^
  -file "%CERTS_DIR%\server.crt"

if errorlevel 1 (
    echo Error exportando certificado
    exit /b 1
)

echo.
echo 3. Creando truststore del cliente...
%KEYTOOL% -importcert ^
  -alias server ^
  -file "%CERTS_DIR%\server.crt" ^
  -keystore "%CERTS_DIR%\truststore.jks" ^
  -storepass %PASSWORD% ^
  -noprompt

if errorlevel 1 (
    echo Error creando truststore
    exit /b 1
)

echo.
echo ======================================
echo OK Certificados generados exitosamente
echo ======================================
echo.
echo Archivos creados en %CERTS_DIR%:
echo   - keystore.jks (para el servidor gRPC)
echo   - truststore.jks (para el cliente gRPC)
echo   - server.crt (certificado publico)
echo.
echo Password: %PASSWORD%
echo Validez: %VALIDITY% dias
echo.

endlocal
