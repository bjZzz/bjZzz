-- Platform layer sys_*
SET NAMES utf8mb4;

CREATE TABLE sys_org (
  id BIGINT NOT NULL PRIMARY KEY,
  org_code VARCHAR(64) NOT NULL,
  org_name VARCHAR(128) NOT NULL,
  org_type VARCHAR(32) NOT NULL COMMENT 'CENTER/SUB_CENTER',
  parent_id BIGINT DEFAULT NULL,
  level_type VARCHAR(32) COMMENT 'PROVINCE/CITY/DISTRICT',
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  org_id BIGINT DEFAULT NULL,
  created_by BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_org_code (org_code, deleted),
  KEY idx_parent (parent_id)
) COMMENT='机构';

CREATE TABLE sys_org_relation (
  id BIGINT NOT NULL PRIMARY KEY,
  org_id BIGINT NOT NULL,
  related_org_id BIGINT NOT NULL,
  relation_type VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  KEY idx_org (org_id)
) COMMENT='机构关系';

CREATE TABLE sys_user (
  id BIGINT NOT NULL PRIMARY KEY,
  username VARCHAR(64) NOT NULL,
  password_hash VARCHAR(256) NOT NULL,
  display_name VARCHAR(128),
  primary_org_id BIGINT,
  status VARCHAR(16) NOT NULL DEFAULT 'ENABLED',
  org_id BIGINT,
  created_by BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_username (username, deleted),
  KEY idx_primary_org (primary_org_id)
) COMMENT='用户';

CREATE TABLE sys_user_org (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  org_id BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_user_org (user_id, org_id, deleted)
) COMMENT='用户机构绑定';

CREATE TABLE sys_role (
  id BIGINT NOT NULL PRIMARY KEY,
  role_code VARCHAR(64) NOT NULL,
  role_name VARCHAR(128) NOT NULL,
  data_scope VARCHAR(32) NOT NULL DEFAULT 'ORG',
  org_id BIGINT,
  created_by BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_by BIGINT,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_role_code (role_code, deleted)
) COMMENT='角色';

CREATE TABLE sys_permission (
  id BIGINT NOT NULL PRIMARY KEY,
  perm_code VARCHAR(128) NOT NULL,
  perm_name VARCHAR(128) NOT NULL,
  module VARCHAR(32),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_perm_code (perm_code, deleted)
) COMMENT='权限';

CREATE TABLE sys_user_role (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_user_role (user_id, role_id, deleted)
) COMMENT='用户角色';

CREATE TABLE sys_role_permission (
  id BIGINT NOT NULL PRIMARY KEY,
  role_id BIGINT NOT NULL,
  perm_id BIGINT NOT NULL,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_role_perm (role_id, perm_id, deleted)
) COMMENT='角色权限';

CREATE TABLE sys_audit_log (
  id BIGINT NOT NULL PRIMARY KEY,
  user_id BIGINT,
  action VARCHAR(64) NOT NULL,
  resource_type VARCHAR(64),
  resource_id VARCHAR(64),
  detail_json JSON,
  ip VARCHAR(64),
  org_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) COMMENT='审计日志';

CREATE TABLE sys_crypto_key (
  id BIGINT NOT NULL PRIMARY KEY,
  key_id VARCHAR(64) NOT NULL,
  algorithm VARCHAR(32) NOT NULL,
  key_version INT NOT NULL DEFAULT 1,
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  effective_from DATETIME,
  org_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted TINYINT NOT NULL DEFAULT 0,
  UNIQUE KEY uk_key_id (key_id, deleted)
) COMMENT='加密密钥';
