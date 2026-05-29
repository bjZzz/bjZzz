-- Staging layer stg_*
SET NAMES utf8mb4;

CREATE TABLE stg_datasource (
  id BIGINT NOT NULL PRIMARY KEY,
  source_code VARCHAR(64) NOT NULL,
  source_name VARCHAR(128) NOT NULL,
  protocol VARCHAR(32) NOT NULL COMMENT 'JDBC/HL7/FHIR/FILE/API',
  config_json JSON NOT NULL,
  org_id BIGINT NOT NULL,
  status VARCHAR(16) DEFAULT 'ACTIVE',
  created_by BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_source_code (source_code, org_id, deleted)
) COMMENT='数据源';

CREATE TABLE stg_sync_job (
  id BIGINT NOT NULL PRIMARY KEY,
  source_id BIGINT NOT NULL,
  schedule_type VARCHAR(16) NOT NULL COMMENT 'T7/T1/NEAR_RT/MANUAL',
  cron_expr VARCHAR(64),
  last_run_at DATETIME,
  last_status VARCHAR(16),
  org_id BIGINT NOT NULL,
  created_by BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_source (source_id)
) COMMENT='同步任务';

CREATE TABLE stg_sync_log (
  id BIGINT NOT NULL PRIMARY KEY,
  job_id BIGINT NOT NULL,
  started_at DATETIME NOT NULL,
  finished_at DATETIME,
  status VARCHAR(16),
  message TEXT,
  org_id BIGINT NOT NULL,
  KEY idx_job (job_id)
) COMMENT='同步日志';

CREATE TABLE stg_batch (
  id BIGINT NOT NULL PRIMARY KEY,
  source_id BIGINT NOT NULL,
  job_id BIGINT,
  org_id BIGINT NOT NULL,
  received_at DATETIME NOT NULL,
  record_count INT DEFAULT 0,
  success_count INT DEFAULT 0,
  fail_count INT DEFAULT 0,
  status VARCHAR(32) NOT NULL,
  error_message TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_status_time (status, received_at),
  KEY idx_org (org_id)
) COMMENT='Staging批次';

CREATE TABLE stg_record (
  id BIGINT NOT NULL PRIMARY KEY,
  batch_id BIGINT NOT NULL,
  domain VARCHAR(32) NOT NULL,
  raw_payload JSON,
  source_ref VARCHAR(128) NOT NULL,
  parse_status VARCHAR(16) DEFAULT 'OK',
  parse_error TEXT,
  org_id BIGINT,
  KEY idx_batch (batch_id),
  KEY idx_source_ref (source_ref, batch_id)
) COMMENT='Staging记录';

CREATE TABLE stg_webhook_subscription (
  id BIGINT NOT NULL PRIMARY KEY,
  endpoint_url VARCHAR(512) NOT NULL,
  secret_hash VARCHAR(128),
  org_id BIGINT NOT NULL,
  status VARCHAR(16) DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_org (org_id)
) COMMENT='Webhook订阅';
