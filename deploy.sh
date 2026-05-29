#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT"

if [ ! -f .env ]; then
  cp .env.example .env
  echo "Created .env from .env.example — please review secrets before production use."
fi

echo "==> Building and starting Nanda stack..."
docker compose up -d --build

echo ""
echo "==> Deployment started. Waiting for health checks..."
docker compose ps

echo ""
echo "Access:"
echo "  Web UI:  http://localhost:${HTTP_PORT:-80}"
echo "  API:     http://localhost:${API_PORT:-8080}/api/v1/health"
echo "  API Doc: http://localhost:${HTTP_PORT:-80}/doc.html"
echo ""
echo "Default login: admin / admin123"
