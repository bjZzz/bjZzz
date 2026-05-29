-- Integration W7: endpoint management, FHIR read, writeback idempotency
SET NAMES utf8mb4;

ALTER TABLE int_writeback_log
  ADD COLUMN client_request_id VARCHAR(64) AFTER endpoint_id,
  ADD UNIQUE KEY uk_writeback_client (client_request_id, org_id);

INSERT INTO sys_permission (id, perm_code, perm_name, module, deleted) VALUES
(70, 'integration:endpoint:manage', '集成端点管理', 'integration', 0),
(71, 'integration:writeback:execute', '外部回写执行', 'integration', 0),
(72, 'integration:fhir:read', 'FHIR读取', 'integration', 0);

INSERT INTO sys_role_permission (id, role_id, perm_id, deleted) VALUES
(70, 1, 70, 0),
(71, 1, 71, 0),
(72, 1, 72, 0);
