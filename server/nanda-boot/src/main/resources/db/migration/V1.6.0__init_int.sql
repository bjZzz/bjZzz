-- Integration layer int_*
SET NAMES utf8mb4;

CREATE TABLE int_endpoint_config (
  id BIGINT NOT NULL PRIMARY KEY,
  endpoint_code VARCHAR(64) NOT NULL,
  endpoint_type VARCHAR(32),
  base_url VARCHAR(512),
  auth_type VARCHAR(32),
  auth_config_json JSON,
  org_id BIGINT,
  status VARCHAR(16) DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='外部端点';

CREATE TABLE int_upload_batch (
  id BIGINT NOT NULL PRIMARY KEY,
  template_type VARCHAR(32) NOT NULL,
  file_name VARCHAR(256),
  file_ref VARCHAR(512),
  stg_batch_id BIGINT,
  org_id BIGINT NOT NULL,
  total_rows INT,
  success_rows INT,
  fail_rows INT,
  status VARCHAR(16) DEFAULT 'PROCESSING',
  client_request_id VARCHAR(64),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_client_req (client_request_id, org_id)
) COMMENT='上传批次';

CREATE TABLE int_upload_error (
  id BIGINT NOT NULL PRIMARY KEY,
  upload_batch_id BIGINT NOT NULL,
  row_num INT NOT NULL,
  error_message VARCHAR(512),
  row_data_json JSON
) COMMENT='上传错误行';

CREATE TABLE int_writeback_log (
  id BIGINT NOT NULL PRIMARY KEY,
  endpoint_id BIGINT,
  payload_json JSON,
  response_status INT,
  response_body TEXT,
  retry_count INT DEFAULT 0,
  status VARCHAR(16),
  org_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='回写日志';
