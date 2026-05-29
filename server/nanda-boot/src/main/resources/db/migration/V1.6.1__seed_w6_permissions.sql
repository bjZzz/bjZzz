-- Seed analytics W6 permissions (risk / statistics / report / dashboard)
INSERT INTO sys_permission (id, perm_code, perm_name, module, deleted) VALUES
(63, 'analytics:risk:execute', '风险模型执行', 'analytics', 0),
(64, 'analytics:stat:execute', '统计分析执行', 'analytics', 0),
(65, 'analytics:report:manage', '评估报告管理', 'analytics', 0),
(66, 'analytics:dashboard:manage', '仪表盘管理', 'analytics', 0);

INSERT INTO sys_role_permission (id, role_id, perm_id, deleted) VALUES
(63, 1, 63, 0), (64, 1, 64, 0), (65, 1, 65, 0), (66, 1, 66, 0);
