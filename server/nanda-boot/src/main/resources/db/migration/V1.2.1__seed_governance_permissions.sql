-- Seed governance permissions + default publish/cleaning rules
INSERT INTO sys_permission (id, perm_code, perm_name, module, deleted) VALUES
(20, 'governance:crf:design', 'CRF设计', 'governance', 0),
(21, 'governance:crf:entry', 'CRF录入', 'governance', 0),
(22, 'governance:dict:read', '字典查看', 'governance', 0),
(23, 'governance:dict:write', '字典编辑', 'governance', 0),
(24, 'governance:publish:execute', '入库发布', 'governance', 0),
(25, 'governance:metadata:read', '元数据查看', 'governance', 0);

INSERT INTO sys_role_permission (id, role_id, perm_id, deleted) VALUES
(20, 1, 20, 0), (21, 1, 21, 0), (22, 1, 22, 0),
(23, 1, 23, 0), (24, 1, 24, 0), (25, 1, 25, 0);

INSERT INTO gov_publish_rule (id, rule_name, specialty_type, inclusion_json, org_id, deleted) VALUES
(1, '默认代谢专病入库', 'METABOLIC', '{"domainEquals":"PATIENT","excludeAbnormal":true}', 1, 0);

INSERT INTO gov_cleaning_rule (id, rule_code, rule_type, rule_config_json, specialty_type, status, org_id, created_at, updated_at, deleted) VALUES
(1, 'DEFAULT_MISSING_NAME', 'MISSING', '{"field":"name","defaultValue":"未知"}', NULL, 'ACTIVE', 1, NOW(), NOW(), 0),
(2, 'DEFAULT_DEDUP', 'DEDUP', '{"mergeKey":"id"}', NULL, 'ACTIVE', 1, NOW(), NOW(), 0);
