param(
    [string]$ip = $(Read-Host "Digite o IP do servidor"),
    [string]$porta = "7070"
)
Write-Host "=========================================="
Write-Host "Cliente de Investimentos B3"
Write-Host "=========================================="
Write-Host ""
Write-Host "Conectando em: $ip`:$porta"
Write-Host ""
Write-Host "Testando conectividade..."
if (Test-Connection -Count 1 -Quiet $ip) {
    Write-Host "[OK] Servidor respondendo"
} else {
    Write-Host "[AVISO] Servidor nao responde ao ping"
}
Write-Host ""
Write-Host "Iniciando cliente..."
Write-Host ""
& mvn -q exec:java "-Dexec.mainClass=br.ufc.ds.trabalho1.app.ClienteMain" "-Dexec.args=$ip $porta"