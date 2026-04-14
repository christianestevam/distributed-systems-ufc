@echo off
REM Script para iniciar o servidor de investimentos B3

setlocal enabledelayedexpansion

set PORTA=7070
if not "%1"=="" set PORTA=%1

echo ==========================================
echo Servidor de Investimentos B3
echo ==========================================
echo.
echo Porta de escuta: %PORTA%
echo.
echo Descobrindo seu IP local...
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| find "IPv4"') do (
    set IP=%%a
    goto :found_ip
)
:found_ip
set IP=%IP: =%
echo Seu IP local e: %IP%
echo.
echo Clientes devem conectar em: %IP%:%PORTA%
echo.
echo Iniciando servidor...
echo.

mvn -q exec:java -Dexec.mainClass=br.ufc.ds.trabalho1.app.ServidorMain -Dexec.args="%PORTA%"
