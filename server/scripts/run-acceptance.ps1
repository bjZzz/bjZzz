$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
Set-Location $Root

Write-Host "==> REQ coverage audit (no DB)"
mvn -pl nanda-boot test -Dtest=ReqCoverageReportTest -q

Write-Host "==> BP-01~09 acceptance (requires MySQL on localhost:3306)"
mvn -pl nanda-boot test -Pacceptance -q

Write-Host "==> Report: nanda-boot/target/acceptance-req-report.json"
