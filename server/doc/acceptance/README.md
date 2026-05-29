# W9 Acceptance Testing

One-phase acceptance suite for **233 REQ** traceability and **BP-01~09** end-to-end flows.

## Prerequisites

1. Start infrastructure:

```bash
docker compose up -d mysql
```

2. Ensure Flyway migrations have run (happens automatically on first Spring Boot start or test run).

3. Default credentials: `admin` / `admin123`, org id `1`.

## Run

### REQ matrix audit (no MySQL required)

```bash
mvn -pl nanda-boot test -Dtest=ReqCoverageReportTest
```

Generates `nanda-boot/target/acceptance-req-report.json` with 233 REQ coverage mapping.

### Full BP acceptance (requires MySQL)

```bash
# Linux/macOS
./scripts/run-acceptance.sh

# Windows
./scripts/run-acceptance.ps1
```

Or manually:

```bash
mvn -pl nanda-boot test -Dtest=ReqCoverageReportTest
mvn -pl nanda-boot test -Pacceptance
```

Acceptance tests are tagged `@Tag("acceptance")` and skipped in default `mvn test`.

## Test structure

| Class | Covers |
| --- | --- |
| `PlatformAcceptanceTest` | PLATFORM (REQ-20-*, REQ-05-07-*) |
| `Bp01IngestionAcceptanceTest` | BP-01 upload → publish → specialty |
| `Bp02CrfSupplementAcceptanceTest` | BP-02 CRF + dual-screen supplement |
| `Bp03QualityAcceptanceTest` | BP-03 QC dashboard/sampling |
| `Bp04SearchExportAcceptanceTest` | BP-04 search + export approval |
| `Bp05ResearchAcceptanceTest` | BP-05 project/cohort/follow-up |
| `Bp06SandboxAcceptanceTest` | BP-06 sandbox + statistics |
| `Bp07RiskReportAcceptanceTest` | BP-07 risk models + PDF |
| `Bp08IntegrationAcceptanceTest` | BP-08 FHIR + writeback |
| `Bp09KnowledgePatient360AcceptanceTest` | BP-09 knowledge + 360 |
| `CrossBpGoldenPathAcceptanceTest` | BP-01→05→04→07→08 golden path |

REQ catalog: `nanda-boot/src/test/resources/acceptance/req-catalog.tsv` (233 entries from `doc/功能需求清单.md`).

## Environment variables

| Variable | Default |
| --- | --- |
| `ACCEPTANCE_DB_URL` | `jdbc:mysql://localhost:3306/nanda?...` |
| `DB_USER` | `nanda` |
| `DB_PASS` | `nanda123` |

## Acceptance criteria mapping (DD-06 §11)

| Criterion | Verification |
| --- | --- |
| 233 REQ tests | `ReqCoverageReportTest` + `@CoversReqGroup` |
| BP-01~09 demo | `Bp0x*AcceptanceTest` + `CrossBpGoldenPathAcceptanceTest` |
| ES/MySQL consistency | MySQL fallback when ES disabled; enable `nanda.elasticsearch.enabled=true` for parity tests |
| Comorbidity refresh <5min | Async via MQ when enabled; BP-01 tests synchronous publish path |
| 10 risk models | `Bp07RiskReportAcceptanceTest` + `GET /risk-models` |
| FHIR Patient/Observation | `Bp08IntegrationAcceptanceTest` |
| CDISC export | Use `exportFormat=CDISC` in BP-04 extension |
