-- Seed integration permissions and writeback idempotency support
ALTER TABLE int_writeback_log
  ADD COLUMN client_request_id VARCHAR(64) NULL AFTER endpoint_id,
  ADD UNIQUE KEY uk_writeback_client_req (client_request_id, org_id);

INSERT INTO sys_permission (id, perm_code, perm_name, module, deleted) VALUES
(70, 'integration:upload:write', '集成上传', 'integration', 0),
(71, 'integration:writeback:execute', '结果回写', 'integration', 0),
(72, 'integration:fhir:read', 'FHIR读取', 'integration', 0),
(73, 'integration:endpoint:manage', '集成端点管理', 'integration', 0);

INSERT INTO sys_role_permission (id, role_id, perm_id, deleted) VALUES
(70, 1, 70, 0), (71, 1, 71, 0), (72, 1, 72, 0), (73, 1, 73, 0);
