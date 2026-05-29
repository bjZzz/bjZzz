-- Research layer res_*
SET NAMES utf8mb4;

CREATE TABLE res_project (
  id BIGINT NOT NULL PRIMARY KEY,
  project_code VARCHAR(64) NOT NULL,
  project_name VARCHAR(256) NOT NULL,
  status VARCHAR(32) NOT NULL DEFAULT 'DRAFT',
  design_json JSON,
  template_code VARCHAR(64),
  pi_user_id BIGINT,
  org_id BIGINT NOT NULL,
  start_date DATE,
  end_date DATE,
  archived_at DATETIME,
  created_by BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_code (project_code, deleted)
) COMMENT='科研项目';

CREATE TABLE res_project_member (
  id BIGINT NOT NULL PRIMARY KEY,
  project_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role_in_project VARCHAR(32),
  org_id BIGINT NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_proj_user (project_id, user_id, deleted)
) COMMENT='项目成员';

CREATE TABLE res_project_archive (
  id BIGINT NOT NULL PRIMARY KEY,
  project_id BIGINT NOT NULL,
  archive_ref VARCHAR(512),
  archived_at DATETIME NOT NULL
) COMMENT='项目归档';

CREATE TABLE res_cohort (
  id BIGINT NOT NULL PRIMARY KEY,
  project_id BIGINT NOT NULL,
  cohort_name VARCHAR(128) NOT NULL,
  cohort_type VARCHAR(32),
  rule_json JSON,
  member_count INT DEFAULT 0,
  org_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_project (project_id)
) COMMENT='科研队列';

CREATE TABLE res_cohort_member (
  id BIGINT NOT NULL PRIMARY KEY,
  cohort_id BIGINT NOT NULL,
  empi_id BIGINT NOT NULL,
  group_label VARCHAR(64),
  enroll_date DATE,
  status VARCHAR(16) DEFAULT 'ACTIVE',
  UNIQUE KEY uk_cohort_empi (cohort_id, empi_id),
  KEY idx_empi (empi_id)
) COMMENT='队列成员';

CREATE TABLE res_cohort_inclusion_rule (
  id BIGINT NOT NULL PRIMARY KEY,
  cohort_id BIGINT NOT NULL,
  rule_json JSON NOT NULL,
  version INT DEFAULT 1,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='纳排规则';

CREATE TABLE res_randomization_record (
  id BIGINT NOT NULL PRIMARY KEY,
  cohort_id BIGINT NOT NULL,
  cohort_member_id BIGINT NOT NULL,
  group_assigned VARCHAR(64),
  randomized_at DATETIME NOT NULL
) COMMENT='随机分组';

CREATE TABLE res_follow_up_plan (
  id BIGINT NOT NULL PRIMARY KEY,
  project_id BIGINT NOT NULL,
  plan_name VARCHAR(128),
  org_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='随访计划';

CREATE TABLE res_follow_up_stage (
  id BIGINT NOT NULL PRIMARY KEY,
  plan_id BIGINT NOT NULL,
  stage_name VARCHAR(64),
  offset_days INT,
  window_days INT,
  sort_order INT
) COMMENT='随访阶段';

CREATE TABLE res_follow_up_task (
  id BIGINT NOT NULL PRIMARY KEY,
  stage_id BIGINT NOT NULL,
  cohort_member_id BIGINT NOT NULL,
  due_date DATE,
  status VARCHAR(16) DEFAULT 'PENDING',
  completed_at DATETIME,
  channel VARCHAR(32),
  KEY idx_due (status, due_date)
) COMMENT='随访任务';

CREATE TABLE res_follow_up_crf_binding (
  id BIGINT NOT NULL PRIMARY KEY,
  stage_id BIGINT NOT NULL,
  form_id BIGINT NOT NULL
) COMMENT='阶段CRF绑定';
