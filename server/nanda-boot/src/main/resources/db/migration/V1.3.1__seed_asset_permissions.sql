-- Seed asset permissions + default EMPI/comorbidity rules
INSERT INTO sys_permission (id, perm_code, perm_name, module, deleted) VALUES
(30, 'asset:empi:read', 'EMPI查看', 'asset', 0),
(31, 'asset:empi:match', 'EMPI匹配', 'asset', 0),
(32, 'asset:specialty:read', '专病库查看', 'asset', 0),
(33, 'asset:specialty:write', '专病库编辑', 'asset', 0),
(34, 'asset:comorbidity:read', '共病库查看', 'asset', 0),
(35, 'asset:comorbidity:write', '共病库编辑', 'asset', 0),
(36, 'asset:qc:read', '质控查看', 'asset', 0),
(37, 'asset:qc:write', '质控操作', 'asset', 0),
(38, 'asset:supplement:write', '双屏补录', 'asset', 0),
(39, 'asset:knowledge:read', '知识库查看', 'asset', 0),
(40, 'asset:knowledge:write', '知识库编辑', 'asset', 0);

INSERT INTO sys_role_permission (id, role_id, perm_id, deleted) VALUES
(30, 1, 30, 0), (31, 1, 31, 0), (32, 1, 32, 0), (33, 1, 33, 0),
(34, 1, 34, 0), (35, 1, 35, 0), (36, 1, 36, 0), (37, 1, 37, 0),
(38, 1, 38, 0), (39, 1, 39, 0), (40, 1, 40, 0);

INSERT INTO empi_match_rule (id, rule_name, rule_config_json, status, org_id, deleted) VALUES
(1, '默认匹配权重', '{"nameWeight":0.35,"phoneWeight":0.25,"addressWeight":0.15,"birthDateWeight":0.15,"idCardWeight":0.10,"autoThreshold":0.85,"crossMatchThreshold":0.99}', 'ACTIVE', 1, 0);

INSERT INTO pub_comorbidity_rule (id, rule_name, expression_json, time_window_json, status, org_id, deleted) VALUES
(1, '代谢+心脑血管共病', '{"specialties":["METABOLIC","CARDIO_CEREBROVASCULAR"],"minCount":2,"labels":["代谢+心脑血管共病"]}', NULL, 'ACTIVE', 1, 0);
