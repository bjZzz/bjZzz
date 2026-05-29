-- Published / EMPI / QC / Knowledge
SET NAMES utf8mb4;

CREATE TABLE empi_master (
  id BIGINT NOT NULL PRIMARY KEY,
  display_name VARCHAR(64),
  gender VARCHAR(8),
  birth_date DATE,
  merge_status VARCHAR(16) DEFAULT 'ACTIVE',
  merged_to_id BIGINT,
  match_confidence DECIMAL(5,4),
  org_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_merge (merge_status)
) COMMENT='EMPI主索引';

CREATE TABLE empi_identifier (
  id BIGINT NOT NULL PRIMARY KEY,
  empi_id BIGINT NOT NULL,
  id_type VARCHAR(32) NOT NULL,
  id_value_enc VARCHAR(512) NOT NULL,
  id_hash VARCHAR(64) NOT NULL,
  source_system VARCHAR(64),
  is_primary TINYINT DEFAULT 0,
  UNIQUE KEY uk_type_hash (id_type, id_hash),
  KEY idx_empi (empi_id)
) COMMENT='患者标识';

CREATE TABLE empi_match_candidate (
  id BIGINT NOT NULL PRIMARY KEY,
  source_record_id BIGINT,
  candidate_empi_id BIGINT,
  match_score DECIMAL(5,4),
  match_features JSON,
  review_status VARCHAR(16) DEFAULT 'PENDING',
  reviewer_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_pending (review_status, created_at)
) COMMENT='匹配候选';

CREATE TABLE empi_match_rule (
  id BIGINT NOT NULL PRIMARY KEY,
  rule_name VARCHAR(128),
  rule_config_json JSON,
  status VARCHAR(16) DEFAULT 'ACTIVE',
  org_id BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='匹配规则';

CREATE TABLE pub_specialty_patient (
  id BIGINT NOT NULL PRIMARY KEY,
  empi_id BIGINT NOT NULL,
  specialty_type VARCHAR(32) NOT NULL,
  org_id BIGINT NOT NULL,
  template_id BIGINT,
  core_fields JSON,
  extended_fields JSON,
  status VARCHAR(16) DEFAULT 'ACTIVE',
  first_diagnosis_date DATE,
  created_by BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_empi_type (empi_id, specialty_type),
  KEY idx_org_type (org_id, specialty_type, deleted)
) COMMENT='专病患者';

CREATE TABLE pub_specialty_medical_record (
  id BIGINT NOT NULL PRIMARY KEY,
  patient_id BIGINT NOT NULL,
  record_type VARCHAR(32),
  content_json JSON,
  org_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_patient (patient_id)
) COMMENT='专病病历';

CREATE TABLE pub_specialty_lab_exam (
  id BIGINT NOT NULL PRIMARY KEY,
  patient_id BIGINT NOT NULL,
  exam_code VARCHAR(64),
  exam_value VARCHAR(128),
  exam_unit VARCHAR(32),
  exam_date DATE,
  org_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_patient (patient_id)
) COMMENT='专病检验';

CREATE TABLE pub_specialty_treatment (
  id BIGINT NOT NULL PRIMARY KEY,
  patient_id BIGINT NOT NULL,
  treatment_json JSON,
  org_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_patient (patient_id)
) COMMENT='专病治疗';

CREATE TABLE pub_specialty_follow_up (
  id BIGINT NOT NULL PRIMARY KEY,
  patient_id BIGINT NOT NULL,
  follow_up_json JSON,
  org_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_patient (patient_id)
) COMMENT='专病随访';

CREATE TABLE pub_specialty_template (
  id BIGINT NOT NULL PRIMARY KEY,
  specialty_type VARCHAR(32) NOT NULL,
  template_json JSON,
  version INT DEFAULT 1,
  org_id BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='专病模板';

CREATE TABLE pub_specialty_attachment (
  id BIGINT NOT NULL PRIMARY KEY,
  patient_id BIGINT NOT NULL,
  file_ref VARCHAR(512),
  org_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='专病附件';

CREATE TABLE pub_comorbidity_rule (
  id BIGINT NOT NULL PRIMARY KEY,
  rule_name VARCHAR(128) NOT NULL,
  expression_json JSON NOT NULL,
  time_window_json JSON,
  status VARCHAR(16) DEFAULT 'ACTIVE',
  org_id BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='共病规则';

CREATE TABLE pub_comorbidity_view (
  id BIGINT NOT NULL PRIMARY KEY,
  rule_id BIGINT NOT NULL,
  empi_id BIGINT NOT NULL,
  specialty_record_ids JSON,
  comorbidity_labels JSON,
  refresh_version INT,
  refreshed_at DATETIME,
  UNIQUE KEY uk_rule_empi (rule_id, empi_id)
) COMMENT='共病视图';

CREATE TABLE pub_knowledge_document (
  id BIGINT NOT NULL PRIMARY KEY,
  title VARCHAR(512),
  doc_type VARCHAR(32),
  file_ref VARCHAR(512),
  org_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='知识库文档';

CREATE TABLE pub_knowledge_citation (
  id BIGINT NOT NULL PRIMARY KEY,
  document_id BIGINT NOT NULL,
  cited_document_id BIGINT,
  citation_text TEXT
) COMMENT='引用';

CREATE TABLE pub_knowledge_author (
  id BIGINT NOT NULL PRIMARY KEY,
  document_id BIGINT NOT NULL,
  author_name VARCHAR(128)
) COMMENT='作者';

CREATE TABLE pub_knowledge_tag (
  id BIGINT NOT NULL PRIMARY KEY,
  document_id BIGINT NOT NULL,
  tag_name VARCHAR(64)
) COMMENT='标签';

CREATE TABLE qc_metric_snapshot (
  id BIGINT NOT NULL PRIMARY KEY,
  metric_type VARCHAR(32),
  metric_value DECIMAL(10,4),
  snapshot_at DATETIME,
  org_id BIGINT NOT NULL
) COMMENT='质控指标快照';

CREATE TABLE qc_sample_batch (
  id BIGINT NOT NULL PRIMARY KEY,
  batch_name VARCHAR(128),
  strategy_json JSON,
  org_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='抽样批次';

CREATE TABLE qc_sample_record (
  id BIGINT NOT NULL PRIMARY KEY,
  batch_id BIGINT NOT NULL,
  patient_id BIGINT,
  org_id BIGINT NOT NULL
) COMMENT='抽样记录';

CREATE TABLE qc_review_task (
  id BIGINT NOT NULL PRIMARY KEY,
  sample_record_id BIGINT,
  status VARCHAR(16),
  reviewer_id BIGINT,
  org_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='复核任务';

CREATE TABLE qc_monthly_report (
  id BIGINT NOT NULL PRIMARY KEY,
  report_month VARCHAR(7),
  report_json JSON,
  org_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='质控月报';

CREATE TABLE pub_data_change_log (
  id BIGINT NOT NULL PRIMARY KEY,
  patient_id BIGINT,
  change_type VARCHAR(32),
  before_json JSON,
  after_json JSON,
  operator_id BIGINT,
  org_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='补录变更日志';
