@echo off
REM Script para conectar cliente ao servidor de investimentos B3

setlocal enabledelayedexpansion

echo ==========================================
echo Cliente de Investimentos B3
echo ==========================================
echo.

if "%1"=="" (
    echo Digite o IP do servidor:
    set /p IP_SERVIDOR=
) else (
    set IP_SERVIDOR=%1
)

set PORTA=7070
if not "%2"=="" set PORTA=%2

echo.
echo Conectando em: %IP_SERVIDOR%:%PORTA%
echo.
echo Testando conectividade...
ping -n 1 "%IP_SERVIDOR%" > nul 2>&1
if %errorlevel%==0 (
    echo [OK] Servidor respondendo
) else (
    echo [AVISO] Servidor nao responde ao ping
)

echo.
echo Iniciando cliente...
echo.

mvn -q exec:java -Dexec.mainClass=br.ufc.ds.trabalho1.app.ClienteMain -Dexec.args="%IP_SERVIDOR% %PORTA%"
