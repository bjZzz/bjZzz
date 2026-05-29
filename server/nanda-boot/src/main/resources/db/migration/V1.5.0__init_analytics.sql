-- Analytics / Index / Sandbox session tables
SET NAMES utf8mb4;

CREATE TABLE ana_search_query (
  id BIGINT NOT NULL PRIMARY KEY,
  query_name VARCHAR(128),
  query_json JSON NOT NULL,
  scope VARCHAR(32),
  scope_id BIGINT,
  user_id BIGINT NOT NULL,
  org_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='检索条件';

CREATE TABLE ana_export_task (
  id BIGINT NOT NULL PRIMARY KEY,
  search_query_id BIGINT,
  export_format VARCHAR(16),
  export_scope_json JSON,
  status VARCHAR(32) DEFAULT 'DRAFT',
  approver_id BIGINT,
  approved_at DATETIME,
  user_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_status (status, created_at)
) COMMENT='导出任务';

CREATE TABLE ana_export_file (
  id BIGINT NOT NULL PRIMARY KEY,
  task_id BIGINT NOT NULL,
  file_ref VARCHAR(512),
  file_size BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='导出文件';

CREATE TABLE idx_search_document (
  id BIGINT NOT NULL PRIMARY KEY,
  empi_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  specialty_types JSON,
  diagnosis_codes JSON,
  lab_values JSON,
  demographics JSON,
  completeness_score DECIMAL(5,2),
  updated_at DATETIME,
  KEY idx_org (org_id),
  KEY idx_empi (empi_id)
) COMMENT='检索索引文档';

CREATE TABLE idx_sync_checkpoint (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  last_sync_at DATETIME,
  checkpoint_json JSON
) COMMENT='索引同步位点';

CREATE TABLE ana_analytics_job (
  id BIGINT NOT NULL PRIMARY KEY,
  job_type VARCHAR(32),
  method_code VARCHAR(64),
  input_json JSON,
  status VARCHAR(16) DEFAULT 'QUEUED',
  sandbox_job_id VARCHAR(64),
  user_id BIGINT,
  org_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_queue (status, created_at)
) COMMENT='分析任务';

CREATE TABLE ana_analytics_result (
  id BIGINT NOT NULL PRIMARY KEY,
  job_id BIGINT NOT NULL,
  result_json JSON,
  chart_refs JSON,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='分析结果';

CREATE TABLE ana_risk_assessment (
  id BIGINT NOT NULL PRIMARY KEY,
  empi_id BIGINT,
  model_code VARCHAR(32),
  input_json JSON,
  result_json JSON,
  risk_level VARCHAR(16),
  assessed_at DATETIME,
  org_id BIGINT
) COMMENT='风险评估';

CREATE TABLE ana_report (
  id BIGINT NOT NULL PRIMARY KEY,
  report_type VARCHAR(32),
  source_id BIGINT,
  file_ref VARCHAR(512),
  status VARCHAR(16),
  user_id BIGINT,
  org_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='评估报告';

CREATE TABLE ana_dashboard (
  id BIGINT NOT NULL PRIMARY KEY,
  dashboard_name VARCHAR(128),
  config_json JSON,
  user_id BIGINT,
  org_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='仪表盘';

CREATE TABLE ana_algorithm_registry (
  id BIGINT NOT NULL PRIMARY KEY,
  algorithm_code VARCHAR(64) NOT NULL,
  algorithm_name VARCHAR(128),
  version VARCHAR(32),
  package_ref VARCHAR(512),
  status VARCHAR(16) DEFAULT 'ACTIVE',
  UNIQUE KEY uk_algo_code (algorithm_code)
) COMMENT='算法注册';

CREATE TABLE ana_script_template (
  id BIGINT NOT NULL PRIMARY KEY,
  template_code VARCHAR(64),
  template_name VARCHAR(128),
  script_content MEDIUMTEXT,
  org_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='脚本模板';

CREATE TABLE ana_sandbox_session (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  workspace_id VARCHAR(64) NOT NULL,
  status VARCHAR(16) DEFAULT 'ACTIVE',
  kernel_status VARCHAR(16) DEFAULT 'IDLE',
  last_active_at DATETIME,
  KEY idx_user (user_id, status)
) COMMENT='沙箱会话';

CREATE TABLE ana_sandbox_dataset (
  id BIGINT NOT NULL PRIMARY KEY,
  dataset_id VARCHAR(64) NOT NULL UNIQUE,
  org_id BIGINT NOT NULL,
  source_type VARCHAR(32),
  minio_path VARCHAR(512),
  row_count INT,
  expires_at DATETIME,
  created_at DATETIME NOT NULL
) COMMENT='沙箱数据集';
