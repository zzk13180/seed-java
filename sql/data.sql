-- ========================================
-- 初始数据
-- ========================================

-- 初始用户 (密码: 123456，BCrypt加密)
-- BCrypt hash 使用 cost factor 10 生成，可通过 https://bcrypt-generator.com/ 验证
INSERT INTO t_user (username, password, nickname, email, status) VALUES 
('admin', '$2a$10$EqKcp1WFKVQISheBxkVJaOqhEuLFDL8MdqUVJKFXPiLJsHrCYsKxW', '超级管理员', 'admin@example.com', 1),
('user', '$2a$10$EqKcp1WFKVQISheBxkVJaOqhEuLFDL8MdqUVJKFXPiLJsHrCYsKxW', '普通用户', 'user@example.com', 1),
('test', '$2a$10$EqKcp1WFKVQISheBxkVJaOqhEuLFDL8MdqUVJKFXPiLJsHrCYsKxW', '测试用户', 'test@example.com', 1);

-- 初始角色
INSERT INTO t_role (code, name, description, status) VALUES 
('ADMIN', '超级管理员', '系统超级管理员，拥有所有权限', 1),
('USER', '普通用户', '普通用户角色', 1),
('GUEST', '访客', '只读访客角色', 1);

-- 用户角色关联
INSERT INTO t_user_role (user_id, role_id) VALUES 
(1, 1),  -- admin -> ADMIN
(1, 2),  -- admin -> USER
(2, 2),  -- user -> USER
(3, 3);  -- test -> GUEST

-- 初始权限 (菜单)
INSERT INTO t_permission (parent_id, code, name, type, path, icon, sort, status) VALUES 
(0, 'system', '系统管理', 'MENU', '/system', 'setting', 1, 1),
(1, 'system:user', '用户管理', 'MENU', '/system/user', 'user', 1, 1),
(1, 'system:role', '角色管理', 'MENU', '/system/role', 'team', 2, 1),
(1, 'system:permission', '权限管理', 'MENU', '/system/permission', 'lock', 3, 1);

-- 初始权限 (按钮/接口)
INSERT INTO t_permission (parent_id, code, name, type, sort, status) VALUES 
(2, 'user:read', '查看用户', 'BUTTON', 1, 1),
(2, 'user:write', '编辑用户', 'BUTTON', 2, 1),
(2, 'user:delete', '删除用户', 'BUTTON', 3, 1),
(3, 'role:read', '查看角色', 'BUTTON', 1, 1),
(3, 'role:write', '编辑角色', 'BUTTON', 2, 1),
(3, 'role:delete', '删除角色', 'BUTTON', 3, 1);

-- 角色权限关联 (ADMIN拥有所有权限)
INSERT INTO t_role_permission (role_id, permission_id) 
SELECT 1, id FROM t_permission;

-- USER角色权限
INSERT INTO t_role_permission (role_id, permission_id) VALUES 
(2, 5),  -- user:read
(2, 6);  -- user:write

-- GUEST角色权限
INSERT INTO t_role_permission (role_id, permission_id) VALUES 
(3, 5);  -- user:read
