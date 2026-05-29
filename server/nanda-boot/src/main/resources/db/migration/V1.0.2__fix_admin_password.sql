-- 修正已迁移库中 admin 密码哈希（admin123）
UPDATE sys_user
SET password_hash = '$2a$10$2U4/ePc8YXglMsroQNuOe./VYmtgULyyaVxuppCLsZezf5vgeX0mi',
    updated_at = NOW()
WHERE username = 'admin' AND deleted = 0;
