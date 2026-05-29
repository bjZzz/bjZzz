-- Seed research + analytics permissions
INSERT INTO sys_permission (id, perm_code, perm_name, module, deleted) VALUES
(50, 'research:project:read', '科研项目查看', 'research', 0),
(51, 'research:project:write', '科研项目编辑', 'research', 0),
(52, 'research:cohort:manage', '队列管理', 'research', 0),
(53, 'research:followup:manage', '随访管理', 'research', 0),
(60, 'analytics:search:execute', '检索执行', 'analytics', 0),
(61, 'analytics:export:create', '导出创建', 'analytics', 0),
(62, 'analytics:export:approve', '导出审核', 'analytics', 0);

INSERT INTO sys_role_permission (id, role_id, perm_id, deleted) VALUES
(50, 1, 50, 0), (51, 1, 51, 0), (52, 1, 52, 0), (53, 1, 53, 0),
(60, 1, 60, 0), (61, 1, 61, 0), (62, 1, 62, 0);
