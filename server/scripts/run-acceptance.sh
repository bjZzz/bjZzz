#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "==> REQ coverage audit (no DB)"
mvn -pl nanda-boot test -Dtest=ReqCoverageReportTest -q

echo "==> BP-01~09 acceptance (requires MySQL on localhost:3306)"
mvn -pl nanda-boot test -Pacceptance -q

echo "==> Report: nanda-boot/target/acceptance-req-report.json"
