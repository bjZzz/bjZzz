-- Seed ingestion permissions for SUPER_ADMIN
INSERT INTO sys_permission (id, perm_code, perm_name, module, deleted) VALUES
(10, 'ingestion:datasource:read', '数据源查看', 'ingestion', 0),
(11, 'ingestion:datasource:write', '数据源编辑', 'ingestion', 0),
(12, 'ingestion:sync:execute', '同步任务执行', 'ingestion', 0),
(13, 'ingestion:staging:read', 'Staging批次查看', 'ingestion', 0);

INSERT INTO sys_role_permission (id, role_id, perm_id, deleted) VALUES
(10, 1, 10, 0),
(11, 1, 11, 0),
(12, 1, 12, 0),
(13, 1, 13, 0);
