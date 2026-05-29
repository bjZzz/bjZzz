-- Governance layer gov_*
SET NAMES utf8mb4;

CREATE TABLE gov_crf_form (
  id BIGINT NOT NULL PRIMARY KEY,
  form_code VARCHAR(64) NOT NULL,
  form_name VARCHAR(128) NOT NULL,
  specialty_type VARCHAR(32),
  version INT NOT NULL DEFAULT 1,
  schema_json JSON NOT NULL,
  score_rules_json JSON,
  status VARCHAR(16) NOT NULL DEFAULT 'DRAFT',
  published_at DATETIME,
  org_id BIGINT,
  created_by BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_form_ver (form_code, version, deleted)
) COMMENT='CRF表单';

CREATE TABLE gov_crf_response (
  id BIGINT NOT NULL PRIMARY KEY,
  form_id BIGINT NOT NULL,
  form_version INT NOT NULL,
  empi_id BIGINT,
  project_id BIGINT,
  answers_json JSON,
  scores_json JSON,
  status VARCHAR(16) DEFAULT 'DRAFT',
  submitted_by BIGINT,
  approved_by BIGINT,
  org_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_empi (empi_id),
  KEY idx_status (status)
) COMMENT='CRF答卷';

CREATE TABLE gov_crf_attachment (
  id BIGINT NOT NULL PRIMARY KEY,
  response_id BIGINT NOT NULL,
  file_name VARCHAR(256),
  file_ref VARCHAR(512),
  org_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='CRF附件';

CREATE TABLE gov_cleaning_rule (
  id BIGINT NOT NULL PRIMARY KEY,
  rule_code VARCHAR(64) NOT NULL,
  rule_type VARCHAR(32) NOT NULL,
  rule_config_json JSON NOT NULL,
  specialty_type VARCHAR(32),
  status VARCHAR(16) DEFAULT 'ACTIVE',
  org_id BIGINT,
  created_by BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='清洗规则';

CREATE TABLE gov_cleaning_rule_template (
  id BIGINT NOT NULL PRIMARY KEY,
  template_code VARCHAR(64) NOT NULL,
  template_name VARCHAR(128),
  rule_config_json JSON,
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='清洗规则模板';

CREATE TABLE gov_cleaning_execution (
  id BIGINT NOT NULL PRIMARY KEY,
  batch_id BIGINT,
  rule_id BIGINT,
  status VARCHAR(16),
  result_json JSON,
  org_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='清洗执行';

CREATE TABLE gov_dict_diagnosis (
  id BIGINT NOT NULL PRIMARY KEY,
  code VARCHAR(64) NOT NULL,
  name_zh VARCHAR(256),
  name_en VARCHAR(256),
  org_id BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_code (code)
) COMMENT='诊断字典';

CREATE TABLE gov_dict_treatment (
  id BIGINT NOT NULL PRIMARY KEY,
  code VARCHAR(64) NOT NULL,
  name_zh VARCHAR(256),
  org_id BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='治疗字典';

CREATE TABLE gov_dict_lab_exam (
  id BIGINT NOT NULL PRIMARY KEY,
  code VARCHAR(64) NOT NULL,
  name_zh VARCHAR(256),
  unit VARCHAR(32),
  org_id BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='检验字典';

CREATE TABLE gov_dict_symptom (
  id BIGINT NOT NULL PRIMARY KEY,
  code VARCHAR(64) NOT NULL,
  name_zh VARCHAR(256),
  org_id BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='症状字典';

CREATE TABLE gov_dict_field_metadata (
  id BIGINT NOT NULL PRIMARY KEY,
  field_code VARCHAR(64) NOT NULL,
  field_name_zh VARCHAR(128),
  data_type VARCHAR(32),
  value_domain_json JSON,
  crf_question_type VARCHAR(32),
  specialty_types JSON,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_field_code (field_code, deleted)
) COMMENT='字段元数据';

CREATE TABLE gov_terminology (
  id BIGINT NOT NULL PRIMARY KEY,
  term_code VARCHAR(64) NOT NULL,
  term_name VARCHAR(256),
  specialty_type VARCHAR(32),
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='术语';

CREATE TABLE gov_term_mapping (
  id BIGINT NOT NULL PRIMARY KEY,
  source_code VARCHAR(64),
  target_code VARCHAR(64),
  mapping_type VARCHAR(32),
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='术语映射';

CREATE TABLE gov_term_synonym (
  id BIGINT NOT NULL PRIMARY KEY,
  term_id BIGINT NOT NULL,
  synonym VARCHAR(256),
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='同义词';

CREATE TABLE gov_publish_rule (
  id BIGINT NOT NULL PRIMARY KEY,
  rule_name VARCHAR(128) NOT NULL,
  specialty_type VARCHAR(32) NOT NULL,
  inclusion_json JSON NOT NULL,
  field_mapping_id BIGINT,
  org_id BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='入库规则';

CREATE TABLE gov_publish_task (
  id BIGINT NOT NULL PRIMARY KEY,
  batch_id BIGINT,
  rule_id BIGINT,
  status VARCHAR(16),
  org_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='发布任务';

CREATE TABLE gov_publish_log (
  id BIGINT NOT NULL PRIMARY KEY,
  task_id BIGINT NOT NULL,
  level VARCHAR(16),
  message TEXT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='发布日志';

CREATE TABLE gov_field_mapping (
  id BIGINT NOT NULL PRIMARY KEY,
  mapping_name VARCHAR(128),
  mapping_json JSON,
  org_id BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='字段映射';

CREATE TABLE gov_subject_domain (
  id BIGINT NOT NULL PRIMARY KEY,
  domain_code VARCHAR(64),
  domain_name VARCHAR(128),
  org_id BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='主题域';

CREATE TABLE gov_metadata_catalog (
  id BIGINT NOT NULL PRIMARY KEY,
  catalog_code VARCHAR(64),
  catalog_name VARCHAR(128),
  parent_id BIGINT,
  org_id BIGINT,
  deleted TINYINT NOT NULL DEFAULT 0
) COMMENT='元数据目录';

CREATE TABLE gov_metadata_lineage_edge (
  id BIGINT NOT NULL PRIMARY KEY,
  source_type VARCHAR(32),
  source_id VARCHAR(64),
  target_type VARCHAR(32),
  target_id VARCHAR(64),
  org_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='血缘边';
