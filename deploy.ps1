$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $Root

if (-not (Test-Path ".env")) {
    Copy-Item ".env.example" ".env"
    Write-Host "Created .env from .env.example — review secrets before production."
}

Write-Host "==> Building and starting Nanda stack..."
docker compose up -d --build

Write-Host ""
Write-Host "==> Status:"
docker compose ps

Write-Host ""
Write-Host "Access:"
Write-Host "  Web UI:  http://localhost:80"
Write-Host "  API:     http://localhost:8080/api/v1/health"
Write-Host "  API Doc: http://localhost/doc.html"
Write-Host ""
Write-Host "Default login: admin / admin123"
