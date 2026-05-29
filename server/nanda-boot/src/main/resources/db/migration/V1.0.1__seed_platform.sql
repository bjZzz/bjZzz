-- Seed: default org, admin user, super admin role
-- Password: admin123 (BCrypt)

INSERT INTO sys_org (id, org_code, org_name, org_type, status, org_id, created_at, updated_at, deleted)
VALUES (1, 'ROOT', '南大共病专病平台', 'CENTER', 'ACTIVE', 1, NOW(), NOW(), 0);

INSERT INTO sys_user (id, username, password_hash, display_name, primary_org_id, status, org_id, created_at, updated_at, deleted)
VALUES (1, 'admin', '$2a$10$2U4/ePc8YXglMsroQNuOe./VYmtgULyyaVxuppCLsZezf5vgeX0mi', '系统管理员', 1, 'ENABLED', 1, NOW(), NOW(), 0);

INSERT INTO sys_user_org (id, user_id, org_id, deleted) VALUES (1, 1, 1, 0);

INSERT INTO sys_role (id, role_code, role_name, data_scope, org_id, created_at, updated_at, deleted)
VALUES (1, 'SUPER_ADMIN', '超级管理员', 'ALL', 1, NOW(), NOW(), 0);

INSERT INTO sys_user_role (id, user_id, role_id, deleted) VALUES (1, 1, 1, 0);

INSERT INTO sys_permission (id, perm_code, perm_name, module, deleted) VALUES
(1, 'platform:org:read', '机构查看', 'platform', 0),
(2, 'platform:org:write', '机构编辑', 'platform', 0),
(3, 'platform:user:read', '用户查看', 'platform', 0),
(4, 'platform:user:write', '用户编辑', 'platform', 0),
(5, 'platform:audit:read', '审计查看', 'platform', 0);

INSERT INTO sys_role_permission (id, role_id, perm_id, deleted) VALUES
(1, 1, 1, 0), (2, 1, 2, 0), (3, 1, 3, 0), (4, 1, 4, 0), (5, 1, 5, 0);
